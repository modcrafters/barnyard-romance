package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.food.Feed;
import net.modcrafters.minecraft.barnyardromance.food.FoodData;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
//? if >=1.21.2
import net.minecraft.world.entity.EntitySpawnReason;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// What a feeding block takes: any plain food that one of its enabled species eats by the mod's diets. Superfoods are
// for feeding by hand. The food rules read an animal, so each species is asked through a sample animal that never
// joins the world.
public final class FeederFoods {
    private record Key(FeederKind kind, Item item) {
    }

    // Hoppers offer the same items over and over, so each answer is worked out once. They hold until the config, the
    // diets, the food files or the tags change, as FoodData's do.
    private static final Map<Key, Boolean> ANSWERS = new ConcurrentHashMap<>();
    private static volatile Object config;
    // Dropped with the answers, which happens on every reload and so at each server start.
    private static final Map<ServerLevel, Map<EntityType<?>, Optional<Animal>>> SAMPLES = new ConcurrentHashMap<>();

    private FeederFoods() {
    }

    public static boolean accepts(ServerLevel level, FeederKind kind, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        BarnyardConfig current = BarnyardConfig.current();
        if (config != current) {
            ANSWERS.clear();
            config = current;
        }
        Key key = new Key(kind, stack.getItem());
        Boolean known = ANSWERS.get(key);
        if (known != null) {
            return known;
        }
        boolean answer = work(level, kind, stack.copyWithCount(1), current);
        ANSWERS.put(key, answer);
        return answer;
    }

    private static boolean work(ServerLevel level, FeederKind kind, ItemStack stack, BarnyardConfig config) {
        for (Holder<EntityType<?>> holder : BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(kind.animals())) {
            EntityType<?> type = holder.value();
            if (!config.speciesEnabled(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString())) {
                continue;
            }
            Optional<Animal> sample = sample(level, type);
            if (sample.isPresent() && FoodData.resolve(sample.get(), stack) instanceof Feed.Eaten eaten && !eaten.superfood()) {
                return true;
            }
        }
        return false;
    }

    private static Optional<Animal> sample(ServerLevel level, EntityType<?> type) {
        return SAMPLES.computeIfAbsent(level, ignored -> new ConcurrentHashMap<>()).computeIfAbsent(type, ignored -> {
            //? if >=1.21.2 {
            Entity entity = type.create(level, EntitySpawnReason.LOAD);
            //?} else
            //Entity entity = type.create(level);
            return entity instanceof Animal animal ? Optional.of(animal) : Optional.empty();
        });
    }

    public static void clear() {
        ANSWERS.clear();
        SAMPLES.clear();
    }
}
