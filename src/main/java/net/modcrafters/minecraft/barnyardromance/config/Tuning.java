package net.modcrafters.minecraft.barnyardromance.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// The numbers behind familiarity, growth, inheritance, fertility, old age, food healing, milk, yield, Vigour, fights,
// feeders, escapes and grumpy pets. Ranges are in blocks.
// The familiarity losses are per hungry day, per milking and per shearing.
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
        double yieldScaleExponent,
        double familiarityHungerLoss,
        double familiarityMilkingCost,
        double familiarityShearingCost,
        double wildFamiliarityCap,
        double wildPetFamiliarityCap,
        double refusalBelow,
        double refusalChance,
        double vigourPerMating,
        double vigourPerEgg,
        double hungryMatingHealth,
        double hungryEggHealth,
        int matingRange,
        double rivalFightChance,
        double roosterFightChance,
        double wildFightChance,
        int fightStopRange,
        int fightTruceTicks,
        int feederRange,
        int mangerFullDays,
        int feederFullDays,
        int troughFullDays,
        int birdFeederFullDays,
        int petBowlFullDays,
        double escapeBelow,
        double escapeChance,
        int sulkRange
) {
    private static final double MAX_FOOD_HEAL = 20.0;
    private static final long MAX_MILK_TICKS = 2_400_000L;
    private static final int MAX_MILKINGS = 16;
    private static final double MAX_YIELD = 4.0;
    private static final double MAX_HEALTH_COST = 20.0;
    private static final long MAX_RANGE = 64L;

    public static final Tuning DEFAULTS = new Tuning(0.2, 0.3, 15_000, 30_000, 24_000, 0.35, 0.45, 0.25, 0.05, 0.25, 0.1, 0.25, 0.5, 0.1, 0.5, 1.0,
            72_000L, 72_000L, 1, 2, 1.25, 1.0, 0.75, 1.0, 0.05, 0.05, 0.05, 0.4, 0.8, 0.5, 0.2,
            0.3, 0.15, 2.0, 1.0, 16, 0.5, 0.25, 0.05, 8, 1000, 16, 2, 2, 2, 2, 2, 0.10, 0.10, 16);

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
                yieldFactor(object, "yieldScaleExponent", DEFAULTS.yieldScaleExponent),
                unit(object, "familiarityHungerLoss", DEFAULTS.familiarityHungerLoss),
                unit(object, "familiarityMilkingCost", DEFAULTS.familiarityMilkingCost),
                unit(object, "familiarityShearingCost", DEFAULTS.familiarityShearingCost),
                unit(object, "wildFamiliarityCap", DEFAULTS.wildFamiliarityCap),
                unit(object, "wildPetFamiliarityCap", DEFAULTS.wildPetFamiliarityCap),
                unit(object, "refusalBelow", DEFAULTS.refusalBelow),
                unit(object, "refusalChance", DEFAULTS.refusalChance),
                unit(object, "vigourPerMating", DEFAULTS.vigourPerMating),
                unit(object, "vigourPerEgg", DEFAULTS.vigourPerEgg),
                Math.clamp(number(object, "hungryMatingHealth", DEFAULTS.hungryMatingHealth), 0.0, MAX_HEALTH_COST),
                Math.clamp(number(object, "hungryEggHealth", DEFAULTS.hungryEggHealth), 0.0, MAX_HEALTH_COST),
                // Configs from before the rename say rivalRange.
                range(object, object.has("matingRange") ? "matingRange" : "rivalRange", DEFAULTS.matingRange),
                unit(object, "rivalFightChance", DEFAULTS.rivalFightChance),
                unit(object, "roosterFightChance", DEFAULTS.roosterFightChance),
                unit(object, "wildFightChance", DEFAULTS.wildFightChance),
                range(object, "fightStopRange", DEFAULTS.fightStopRange),
                (int) Math.clamp(Math.round(number(object, "fightTruceTicks", DEFAULTS.fightTruceTicks)), 0L, Integer.MAX_VALUE),
                range(object, "feederRange", DEFAULTS.feederRange),
                (int) Math.clamp(Math.round(number(object, "mangerFullDays", DEFAULTS.mangerFullDays)), 1L, 365L),
                (int) Math.clamp(Math.round(number(object, "feederFullDays", DEFAULTS.feederFullDays)), 1L, 365L),
                (int) Math.clamp(Math.round(number(object, "troughFullDays", DEFAULTS.troughFullDays)), 1L, 365L),
                (int) Math.clamp(Math.round(number(object, "birdFeederFullDays", DEFAULTS.birdFeederFullDays)), 1L, 365L),
                (int) Math.clamp(Math.round(number(object, "petBowlFullDays", DEFAULTS.petBowlFullDays)), 1L, 365L),
                unit(object, "escapeBelow", DEFAULTS.escapeBelow),
                unit(object, "escapeChance", DEFAULTS.escapeChance),
                range(object, "sulkRange", DEFAULTS.sulkRange)
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
        object.addProperty("familiarityHungerLoss", familiarityHungerLoss);
        object.addProperty("familiarityMilkingCost", familiarityMilkingCost);
        object.addProperty("familiarityShearingCost", familiarityShearingCost);
        object.addProperty("wildFamiliarityCap", wildFamiliarityCap);
        object.addProperty("wildPetFamiliarityCap", wildPetFamiliarityCap);
        object.addProperty("refusalBelow", refusalBelow);
        object.addProperty("refusalChance", refusalChance);
        object.addProperty("vigourPerMating", vigourPerMating);
        object.addProperty("vigourPerEgg", vigourPerEgg);
        object.addProperty("hungryMatingHealth", hungryMatingHealth);
        object.addProperty("hungryEggHealth", hungryEggHealth);
        object.addProperty("matingRange", matingRange);
        object.addProperty("rivalFightChance", rivalFightChance);
        object.addProperty("roosterFightChance", roosterFightChance);
        object.addProperty("wildFightChance", wildFightChance);
        object.addProperty("fightStopRange", fightStopRange);
        object.addProperty("fightTruceTicks", fightTruceTicks);
        object.addProperty("feederRange", feederRange);
        object.addProperty("mangerFullDays", mangerFullDays);
        object.addProperty("feederFullDays", feederFullDays);
        object.addProperty("troughFullDays", troughFullDays);
        object.addProperty("birdFeederFullDays", birdFeederFullDays);
        object.addProperty("petBowlFullDays", petBowlFullDays);
        object.addProperty("escapeBelow", escapeBelow);
        object.addProperty("escapeChance", escapeChance);
        object.addProperty("sulkRange", sulkRange);
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

    private static int range(JsonObject object, String name, int fallback) {
        return (int) Math.clamp(Math.round(number(object, name, fallback)), 1L, MAX_RANGE);
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
