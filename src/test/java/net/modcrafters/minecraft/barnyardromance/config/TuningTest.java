package net.modcrafters.minecraft.barnyardromance.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TuningTest {
    @Test
    void defaultsMatchTodaysBehaviour() {
        assertEquals(Tuning.DEFAULTS, BarnyardConfig.defaults().tuning());
        assertEquals(new Tuning(0.2, 0.3, 15_000, 30_000, 24_000, 0.35, 0.45, 0.25, 0.05, 0.25, 0.1, 0.25, 0.5, 0.1, 0.5, 1.0,
                72_000L, 72_000L, 1, 2, 1.25, 1.0, 0.75, 1.0, 0.05, 0.05, 0.05, 0.4, 0.8, 0.5, 0.2,
                0.3, 0.15, 2.0, 1.0, 16, 0.5, 0.25, 0.05, 8, 1000, 16, 0.10, 0.10), Tuning.DEFAULTS);
    }

    @Test
    void feederAndEscapeNumbersRoundTrip() {
        Tuning tuning = BarnyardConfig.parse("""
                { "tuning": { "feederRange": 24, "escapeBelow": 0.2, "escapeChance": 0.5 } }
                """).tuning();

        assertEquals(24, tuning.feederRange());
        assertEquals(0.2, tuning.escapeBelow(), 1.0e-9);
        assertEquals(0.5, tuning.escapeChance(), 1.0e-9);
        assertEquals(tuning, BarnyardConfig.parse(BarnyardConfig.parse("{ \"tuning\": { \"feederRange\": 24, \"escapeBelow\": 0.2, \"escapeChance\": 0.5 } }").toJson()).tuning());
        assertEquals(Tuning.DEFAULTS, BarnyardConfig.parse(BarnyardConfig.defaults().toJson()).tuning());
    }

    @Test
    void wildCaughtTuningIsReadWrittenAndClamped() {
        Tuning tuning = BarnyardConfig.parse("""
                { "tuning": { "wildFamiliarityCap": 0.35, "wildPetFamiliarityCap": 2, "refusalBelow": 0.6, "refusalChance": -1 } }
                """).tuning();

        assertEquals(0.35, tuning.wildFamiliarityCap(), 1.0e-9);
        assertEquals(1.0, tuning.wildPetFamiliarityCap(), 1.0e-9);
        assertEquals(0.6, tuning.refusalBelow(), 1.0e-9);
        assertEquals(0.0, tuning.refusalChance(), 1.0e-9);
        assertEquals(Tuning.DEFAULTS, BarnyardConfig.parse(BarnyardConfig.defaults().toJson()).tuning());
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
    void territorialDefaults() {
        Tuning tuning = Tuning.DEFAULTS;
        assertEquals(0.3, tuning.vigourPerMating(), 1.0e-9);
        assertEquals(0.15, tuning.vigourPerEgg(), 1.0e-9);
        assertEquals(2.0, tuning.hungryMatingHealth(), 1.0e-9);
        assertEquals(1.0, tuning.hungryEggHealth(), 1.0e-9);
        assertEquals(16, tuning.rivalRange());
        assertEquals(0.5, tuning.rivalFightChance(), 1.0e-9);
        assertEquals(0.25, tuning.roosterFightChance(), 1.0e-9);
        assertEquals(0.05, tuning.wildFightChance(), 1.0e-9);
        assertEquals(8, tuning.fightStopRange());
        assertEquals(1000, tuning.fightTruceTicks());
    }

    @Test
    void territorialTuningIsReadAndClamped() {
        Tuning tuning = BarnyardConfig.parse("""
                { "tuning": { "vigourPerMating": 2, "vigourPerEgg": 0.2, "hungryMatingHealth": 50, "hungryEggHealth": -3,
                  "rivalRange": 500, "rivalFightChance": -1, "roosterFightChance": 0.75, "wildFightChance": 3,
                  "fightStopRange": 0, "fightTruceTicks": -20 } }
                """).tuning();

        assertEquals(1.0, tuning.vigourPerMating(), 1.0e-9);
        assertEquals(0.2, tuning.vigourPerEgg(), 1.0e-9);
        assertEquals(20.0, tuning.hungryMatingHealth(), 1.0e-9);
        assertEquals(0.0, tuning.hungryEggHealth(), 1.0e-9);
        assertEquals(64, tuning.rivalRange());
        assertEquals(0.0, tuning.rivalFightChance(), 1.0e-9);
        assertEquals(0.75, tuning.roosterFightChance(), 1.0e-9);
        assertEquals(1.0, tuning.wildFightChance(), 1.0e-9);
        assertEquals(1, tuning.fightStopRange());
        assertEquals(0, tuning.fightTruceTicks());
    }

    @Test
    void tuningRoundTripsThroughJson() {
        assertEquals(Tuning.DEFAULTS, BarnyardConfig.parse(BarnyardConfig.defaults().toJson()).tuning());
    }

    @Test
    void familiarityLossesDefaultToFivePercent() {
        Tuning tuning = BarnyardConfig.parse("{}").tuning();

        assertEquals(0.05, tuning.familiarityHungerLoss(), 1.0e-9);
        assertEquals(0.05, tuning.familiarityMilkingCost(), 1.0e-9);
        assertEquals(0.05, tuning.familiarityShearingCost(), 1.0e-9);
    }

    @Test
    void familiarityLossesParseAndClamp() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "tuning": { "familiarityHungerLoss": 0.03, "familiarityMilkingCost": 2, "familiarityShearingCost": -1 } }
                """);
        Tuning tuning = config.tuning();

        assertEquals(0.03, tuning.familiarityHungerLoss(), 1.0e-9);
        assertEquals(1.0, tuning.familiarityMilkingCost(), 1.0e-9);
        assertEquals(0.0, tuning.familiarityShearingCost(), 1.0e-9);
        assertEquals(tuning, BarnyardConfig.parse(config.toJson()).tuning());
    }
}
