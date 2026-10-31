package net.modcrafters.minecraft.barnyardromance.food;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// The diets and food files from the loaded datapacks. Server side only; reloads replace them.
public final class FoodData {
    private static volatile Map<String, Diet> diets = Map.of();
    private static volatile List<FoodFile> foods = List.of();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private FoodData() {
    }

    public static void replaceDiets(Map<String, Diet> loaded) {
        diets = Map.copyOf(loaded);
        WARNED.clear();
    }

    public static void replaceFoods(List<FoodFile> loaded) {
        foods = List.copyOf(loaded);
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

    public static boolean eats(Animal animal, ItemStack stack) {
        return !stack.isEmpty() && resolve(animal, stack) instanceof Feed.Eaten;
    }
}
