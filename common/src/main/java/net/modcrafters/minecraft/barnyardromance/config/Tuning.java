package net.modcrafters.minecraft.barnyardromance.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// The numbers behind familiarity, growth, inheritance, fertility and old age.
public record Tuning(
        double familiarityGain,
        double familiarityThreshold,
        long feedCooldownTicks,
        int growthFastTicks,
        int growthSlowTicks,
        int growthWildTicks,
        double improveBaseChance,
        double improveNutritionChance,
        double improveStep,
        double inheritSpread,
        double fertilityCostPerBirth,
        double fertilityRecoveryPerDay,
        double fertilityMinToConceive,
        double oldFertilityFactor,
        double deathSpread
) {
    public static final Tuning DEFAULTS = new Tuning(0.2, 0.3, 24_000L, 15_000, 30_000, 24_000, 0.35, 0.45, 0.25, 0.05, 0.25, 0.1, 0.25, 0.5, 0.1);

    // A bad or missing value keeps its default; this never throws.
    static Tuning from(JsonObject object) {
        int fast = ticks(object, "growthFastTicks", DEFAULTS.growthFastTicks);
        int slow = ticks(object, "growthSlowTicks", DEFAULTS.growthSlowTicks);
        if (fast > slow) {
            int swap = fast;
            fast = slow;
            slow = swap;
        }
        int wild = Math.clamp(ticks(object, "growthWildTicks", DEFAULTS.growthWildTicks), fast, slow);
        return new Tuning(
                unit(object, "familiarityGain", DEFAULTS.familiarityGain),
                unit(object, "familiarityThreshold", DEFAULTS.familiarityThreshold),
                Math.max(1L, Math.round(number(object, "feedCooldownTicks", DEFAULTS.feedCooldownTicks))),
                fast,
                slow,
                wild,
                unit(object, "improveBaseChance", DEFAULTS.improveBaseChance),
                unit(object, "improveNutritionChance", DEFAULTS.improveNutritionChance),
                unit(object, "improveStep", DEFAULTS.improveStep),
                unit(object, "inheritSpread", DEFAULTS.inheritSpread),
                unit(object, "fertilityCostPerBirth", DEFAULTS.fertilityCostPerBirth),
                unit(object, "fertilityRecoveryPerDay", DEFAULTS.fertilityRecoveryPerDay),
                unit(object, "fertilityMinToConceive", DEFAULTS.fertilityMinToConceive),
                unit(object, "oldFertilityFactor", DEFAULTS.oldFertilityFactor),
                unit(object, "deathSpread", DEFAULTS.deathSpread)
        );
    }

    JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("familiarityGain", familiarityGain);
        object.addProperty("familiarityThreshold", familiarityThreshold);
        object.addProperty("feedCooldownTicks", feedCooldownTicks);
        object.addProperty("growthFastTicks", growthFastTicks);
        object.addProperty("growthSlowTicks", growthSlowTicks);
        object.addProperty("growthWildTicks", growthWildTicks);
        object.addProperty("improveBaseChance", improveBaseChance);
        object.addProperty("improveNutritionChance", improveNutritionChance);
        object.addProperty("improveStep", improveStep);
        object.addProperty("inheritSpread", inheritSpread);
        object.addProperty("fertilityCostPerBirth", fertilityCostPerBirth);
        object.addProperty("fertilityRecoveryPerDay", fertilityRecoveryPerDay);
        object.addProperty("fertilityMinToConceive", fertilityMinToConceive);
        object.addProperty("oldFertilityFactor", oldFertilityFactor);
        object.addProperty("deathSpread", deathSpread);
        return object;
    }

    private static double unit(JsonObject object, String name, double fallback) {
        return Math.clamp(number(object, name, fallback), 0.0, 1.0);
    }

    private static int ticks(JsonObject object, String name, int fallback) {
        return (int) Math.clamp(Math.round(number(object, name, fallback)), 1L, Integer.MAX_VALUE);
    }

    private static double number(JsonObject object, String name, double fallback) {
        JsonElement element = object.get(name);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return fallback;
        }
        double value = element.getAsDouble();
        return Double.isFinite(value) ? value : fallback;
    }
}
