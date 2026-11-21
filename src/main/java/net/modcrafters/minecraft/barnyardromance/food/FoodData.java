package net.modcrafters.minecraft.barnyardromance.food;

import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.feed.api.FeedApi;
import net.modcrafters.minecraft.barnyardromance.core.ServerScoped;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
//? if >=1.21.2
import net.minecraft.world.entity.EntitySpawnReason;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

// The diets and food files from the loaded datapacks. Server side only; reloads replace them.
public final class FoodData {
    private static volatile Map<String, Diet> diets = Map.of();
    private static volatile List<FoodFile> foods = List.of();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();
    // What each species makes of each item. Vanilla's tempt goal asks for every animal, every player in the dimension
    // and both hands about every other tick, so the answer is worked out once. It depends on the item, never the stack:
    // the stack's own food component only matters for healing, which is read from the stack after this.
    private static final FeedCache<FeedKey> FEEDS = new FeedCache<>();

    private record FeedKey(String species, Item item) {
    }

    // Each species' menu for the herd book. Working one out asks the species about every item, so it is kept until the
    // config, the diets, the food files or the tags change.
    private static final Map<String, List<String>> MENUS = ServerScoped.track(new ConcurrentHashMap<>());
    private static volatile Object menusConfig;

    private FoodData() {
    }

    public static void replaceDiets(Map<String, Diet> loaded) {
        // Sorted by id once here, the order FoodRules.dietFor reads them in.
        diets = Collections.unmodifiableSortedMap(new TreeMap<>(loaded));
        WARNED.clear();
        FEEDS.clear();
        MENUS.clear();
        FeaturesRepository.get(FeedApi.class).foodsChanged();
    }

    public static void replaceFoods(List<FoodFile> loaded) {
        foods = List.copyOf(loaded);
        FEEDS.clear();
        MENUS.clear();
        FeaturesRepository.get(FeedApi.class).foodsChanged();
    }

    // Tags were loaded or reloaded, which changes which items match a diet or a food file.
    public static void tagsReloaded() {
        FEEDS.clear();
        MENUS.clear();
        FeaturesRepository.get(FeedApi.class).foodsChanged();
    }

    public static List<FoodFile> foods() {
        return foods;
    }

    public static Map<String, Diet> diets() {
        return diets;
    }

    public static Optional<Map.Entry<String, Diet>> dietFor(String speciesId) {
        String override = BarnyardConfig.current().dietOverride(speciesId);
        if (override != null && !diets.containsKey(override) && WARNED.add(speciesId)) {
            Constants.LOG.warn("{} asks for diet {}, which isn't loaded; using the diet that lists it", speciesId, override);
        }
        return FoodRules.dietFor(speciesId, override, diets);
    }

    public static Feed resolve(Animal animal, ItemStack stack) {
        String speciesId = AnimalSex.speciesId(animal);
        return FEEDS.get(new FeedKey(speciesId, stack.getItem()), BarnyardConfig.current(), key -> work(animal, stack, speciesId));
    }

    private static Feed work(Animal animal, ItemStack stack, String speciesId) {
        if (!BarnyardConfig.current().speciesEnabled(speciesId)) {
            return Feed.NOT_FOOD;
        }
        Optional<Map.Entry<String, Diet>> diet = dietFor(speciesId);
        return FoodRules.resolve(
                speciesId,
                diet.map(Map.Entry::getKey).orElse(null),
                diet.map(Map.Entry::getValue).orElse(null),
                foods,
                new MinecraftItemFacts(animal, stack)
        );
    }

    // What the species eats, best first (FoodRules.menu); empty for a species that isn't tracked or can't be made.
    public static List<String> menu(ServerLevel level, String speciesId) {
        BarnyardConfig current = BarnyardConfig.current();
        if (menusConfig != current) {
            MENUS.clear();
            menusConfig = current;
        }
        return MENUS.computeIfAbsent(speciesId, ignored -> workMenu(level, speciesId, current));
    }

    private static List<String> workMenu(ServerLevel level, String speciesId, BarnyardConfig config) {
        if (!config.speciesEnabled(speciesId)) {
            return List.of();
        }
        Identifier id = Identifier.tryParse(speciesId);
        Entity entity = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                //? if >=1.21.2 {
                .map(type -> type.create(level, EntitySpawnReason.LOAD))
                //?} else
                //.map(type -> type.create(level))
                .orElse(null);
        if (!(entity instanceof Animal sample)) {
            return List.of();
        }
        // The sample never joins the world; it only answers isFood.
        Map<String, ItemFacts> vanilla = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (!stack.isEmpty() && sample.isFood(stack)) {
                vanilla.put(BuiltInRegistries.ITEM.getKey(item).toString(), new MinecraftItemFacts(sample, stack));
            }
        }
        Optional<Map.Entry<String, Diet>> diet = dietFor(speciesId);
        return List.copyOf(FoodRules.menu(speciesId, diet.map(Map.Entry::getKey).orElse(null), diet.map(Map.Entry::getValue).orElse(null),
                foods, vanilla));
    }

    public static boolean eats(Animal animal, ItemStack stack) {
        return !stack.isEmpty() && resolve(animal, stack) instanceof Feed.Eaten;
    }
}
