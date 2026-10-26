package net.modcrafters.minecraft.barnyardromance.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TuningTest {
    @Test
    void defaultsMatchTodaysBehaviour() {
        assertEquals(Tuning.DEFAULTS, BarnyardConfig.defaults().tuning());
        assertEquals(new Tuning(0.2, 0.3, 24_000L, 15_000, 30_000, 24_000, 0.35, 0.45, 0.25, 0.05, 0.25, 0.1, 0.25, 0.5, 0.1, 0.5, 1.0,
                72_000L, 72_000L, 1, 2, 1.25, 1.0, 0.75, 1.0), Tuning.DEFAULTS);
    }

    @Test
    void parsedTuningOverridesDefaults() {
        Tuning tuning = BarnyardConfig.parse("""
                { "tuning": { "familiarityThreshold": 0.5 } }
                """).tuning();

        assertEquals(0.5, tuning.familiarityThreshold(), 1.0e-9);
        assertEquals(0.2, tuning.familiarityGain(), 1.0e-9);
    }

    @Test
    void badTuningIsClamped() {
        Tuning tuning = BarnyardConfig.parse("""
                { "tuning": { "familiarityGain": 7, "growthFastTicks": 40000, "growthSlowTicks": 10000,
                  "fertilityCostPerBirth": -1, "fertilityRecoveryPerDay": 4, "fertilityMinToConceive": 2,
                  "oldFertilityFactor": 3, "deathSpread": -0.5, "foodHealFactor": 2, "foodHealDefault": 99 } }
                """).tuning();

        assertEquals(1.0, tuning.familiarityGain(), 1.0e-9);
        assertEquals(10_000, tuning.growthFastTicks());
        assertEquals(40_000, tuning.growthSlowTicks());
        assertEquals(24_000, tuning.growthWildTicks());
        assertEquals(0.0, tuning.fertilityCostPerBirth(), 1.0e-9);
        assertEquals(1.0, tuning.fertilityRecoveryPerDay(), 1.0e-9);
        assertEquals(1.0, tuning.fertilityMinToConceive(), 1.0e-9);
        assertEquals(1.0, tuning.oldFertilityFactor(), 1.0e-9);
        assertEquals(0.0, tuning.deathSpread(), 1.0e-9);
        assertEquals(1.0, tuning.foodHealFactor(), 1.0e-9);
        assertEquals(20.0, tuning.foodHealDefault(), 1.0e-9);
    }

    @Test
    void badProductTuningIsClamped() {
        Tuning tuning = BarnyardConfig.parse("""
                { "tuning": { "lactationTicks": 0, "milkKeepsFlowingTicks": 9000000, "milkingsPerDaySmall": -3,
                  "milkingsPerDayLarge": 40, "maleYield": 9, "femaleYield": -1, "oldYieldFactor": 5, "yieldScaleExponent": -2 } }
                """).tuning();

        assertEquals(1L, tuning.lactationTicks());
        assertEquals(2_400_000L, tuning.milkKeepsFlowingTicks());
        assertEquals(0, tuning.milkingsPerDaySmall());
        assertEquals(16, tuning.milkingsPerDayLarge());
        assertEquals(4.0, tuning.maleYield(), 1.0e-9);
        assertEquals(0.0, tuning.femaleYield(), 1.0e-9);
        assertEquals(4.0, tuning.oldYieldFactor(), 1.0e-9);
        assertEquals(0.0, tuning.yieldScaleExponent(), 1.0e-9);
    }

    @Test
    void tuningRoundTripsThroughJson() {
        assertEquals(Tuning.DEFAULTS, BarnyardConfig.parse(BarnyardConfig.defaults().toJson()).tuning());
    }
}
