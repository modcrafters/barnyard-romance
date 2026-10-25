package net.modcrafters.minecraft.barnyardromance.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TuningTest {
    @Test
    void defaultsMatchTodaysBehaviour() {
        assertEquals(Tuning.DEFAULTS, BarnyardConfig.defaults().tuning());
        assertEquals(new Tuning(0.2, 0.3, 24_000L, 15_000, 30_000, 24_000, 0.35, 0.45, 0.25, 0.05, 0.25, 0.1, 0.25), Tuning.DEFAULTS);
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
                  "fertilityCostPerBirth": -1, "fertilityRecoveryPerDay": 4, "fertilityMinToConceive": 2 } }
                """).tuning();

        assertEquals(1.0, tuning.familiarityGain(), 1.0e-9);
        assertEquals(10_000, tuning.growthFastTicks());
        assertEquals(40_000, tuning.growthSlowTicks());
        assertEquals(24_000, tuning.growthWildTicks());
        assertEquals(0.0, tuning.fertilityCostPerBirth(), 1.0e-9);
        assertEquals(1.0, tuning.fertilityRecoveryPerDay(), 1.0e-9);
        assertEquals(1.0, tuning.fertilityMinToConceive(), 1.0e-9);
    }

    @Test
    void tuningRoundTripsThroughJson() {
        assertEquals(Tuning.DEFAULTS, BarnyardConfig.parse(BarnyardConfig.defaults().toJson()).tuning());
    }
}
