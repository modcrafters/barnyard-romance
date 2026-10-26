package net.modcrafters.minecraft.barnyardromance.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// The numbers behind familiarity, growth, inheritance, fertility, old age, food healing, milk and yield.
public record Tuning(
        double familiarityGain,
        double familiarityThreshold,
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
        double deathSpread,
        double foodHealFactor,
        double foodHealDefault,
        long lactationTicks,
        long milkKeepsFlowingTicks,
        int milkingsPerDaySmall,
        int milkingsPerDayLarge,
        double maleYield,
        double femaleYield,
        double oldYieldFactor,
        double yieldScaleExponent
) {
    private static final double MAX_FOOD_HEAL = 20.0;
    private static final long MAX_MILK_TICKS = 2_400_000L;
    private static final int MAX_MILKINGS = 16;
    private static final double MAX_YIELD = 4.0;

    public static final Tuning DEFAULTS = new Tuning(0.2, 0.3, 15_000, 30_000, 24_000, 0.35, 0.45, 0.25, 0.05, 0.25, 0.1, 0.25, 0.5, 0.1, 0.5, 1.0,
            72_000L, 72_000L, 1, 2, 1.25, 1.0, 0.75, 1.0);

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
                unit(object, "deathSpread", DEFAULTS.deathSpread),
                unit(object, "foodHealFactor", DEFAULTS.foodHealFactor),
                Math.clamp(number(object, "foodHealDefault", DEFAULTS.foodHealDefault), 0.0, MAX_FOOD_HEAL),
                milkTicks(object, "lactationTicks", DEFAULTS.lactationTicks),
                milkTicks(object, "milkKeepsFlowingTicks", DEFAULTS.milkKeepsFlowingTicks),
                milkings(object, "milkingsPerDaySmall", DEFAULTS.milkingsPerDaySmall),
                milkings(object, "milkingsPerDayLarge", DEFAULTS.milkingsPerDayLarge),
                yieldFactor(object, "maleYield", DEFAULTS.maleYield),
                yieldFactor(object, "femaleYield", DEFAULTS.femaleYield),
                yieldFactor(object, "oldYieldFactor", DEFAULTS.oldYieldFactor),
                yieldFactor(object, "yieldScaleExponent", DEFAULTS.yieldScaleExponent)
        );
    }

    JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("familiarityGain", familiarityGain);
        object.addProperty("familiarityThreshold", familiarityThreshold);
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
        object.addProperty("foodHealFactor", foodHealFactor);
        object.addProperty("foodHealDefault", foodHealDefault);
        object.addProperty("lactationTicks", lactationTicks);
        object.addProperty("milkKeepsFlowingTicks", milkKeepsFlowingTicks);
        object.addProperty("milkingsPerDaySmall", milkingsPerDaySmall);
        object.addProperty("milkingsPerDayLarge", milkingsPerDayLarge);
        object.addProperty("maleYield", maleYield);
        object.addProperty("femaleYield", femaleYield);
        object.addProperty("oldYieldFactor", oldYieldFactor);
        object.addProperty("yieldScaleExponent", yieldScaleExponent);
        return object;
    }

    private static double unit(JsonObject object, String name, double fallback) {
        return Math.clamp(number(object, name, fallback), 0.0, 1.0);
    }

    private static long milkTicks(JsonObject object, String name, long fallback) {
        return Math.clamp(Math.round(number(object, name, fallback)), 1L, MAX_MILK_TICKS);
    }

    private static int milkings(JsonObject object, String name, int fallback) {
        return (int) Math.clamp(Math.round(number(object, name, fallback)), 0L, MAX_MILKINGS);
    }

    private static double yieldFactor(JsonObject object, String name, double fallback) {
        return Math.clamp(number(object, name, fallback), 0.0, MAX_YIELD);
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
