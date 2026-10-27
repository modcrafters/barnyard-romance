package net.modcrafters.minecraft.barnyardromance.config;

import net.modcrafters.minecraft.barnyardromance.age.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.sex.ScaleRoll;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SexRoll;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BarnyardConfigTest {
    @Test
    void emptyConfigUsesTheDesignDefaults() {
        BarnyardConfig config = BarnyardConfig.parse("{}");

        assertTrue(config.speciesEnabled("minecraft:cow"));
        assertEquals(Sex.FEMALE, SexRoll.roll(0.49, config.femaleRatio()));
        assertEquals(Sex.MALE, SexRoll.roll(0.5, config.femaleRatio()));
        SizeSettings size = config.sizeFor("minecraft:cow");
        assertEquals(0.70, ScaleRoll.roll(Sex.FEMALE, size, 0.0), 1.0e-9);
        assertEquals(1.30, ScaleRoll.roll(Sex.MALE, size, 1.0), 1.0e-9);
    }

    @Test
    void dietOverrideReadsAndWrites() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "species": { "minecraft:pig": { "diet": "barnyard_romance:grazer" } } }
                """);

        assertEquals("barnyard_romance:grazer", config.dietOverride("minecraft:pig"));
        assertEquals(null, config.dietOverride("minecraft:cow"));
        assertEquals("barnyard_romance:grazer", BarnyardConfig.parse(config.toJson()).dietOverride("minecraft:pig"));
        assertThrows(IllegalArgumentException.class, () -> BarnyardConfig.parse("""
                { "species": { "minecraft:pig": { "diet": 3 } } }
                """));
    }

    @Test
    void speciesEntriesOverrideOnlyTheFieldsTheySet() {
        BarnyardConfig config = BarnyardConfig.parse("""
                {
                  "species": {
                    "minecraft:chicken": { "femaleScale": 0.8, "scaleVariation": 0.01 },
                    "minecraft:cow": { "enabled": false, "maleScale": 1.4 }
                  }
                }
                """);

        assertFalse(config.speciesEnabled("minecraft:cow"));
        assertTrue(config.speciesEnabled("minecraft:chicken"));
        assertEquals(new SizeSettings(0.8, 1.2, 0.01), config.sizeFor("minecraft:chicken"));
        assertEquals(new SizeSettings(0.8, 1.4, 0.1), config.sizeFor("minecraft:cow"));
        assertEquals(new SizeSettings(0.8, 1.2, 0.1), config.sizeFor("minecraft:pig"));
    }

    @Test
    void turningTheModOffDisablesEverySpecies() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "enabled": false, "species": { "minecraft:pig": { "enabled": true } } }
                """);

        assertFalse(config.speciesEnabled("minecraft:pig"));
    }

    @Test
    void outOfRangeNumbersAreClampedBeforeTheyAffectRolls() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "femaleRatio": 2, "scaleVariation": -3, "femaleScale": 0.9 }
                """);

        assertEquals(Sex.FEMALE, SexRoll.roll(0.99, config.femaleRatio()));
        SizeSettings size = config.sizeFor("minecraft:sheep");
        assertEquals(0.9, ScaleRoll.roll(Sex.FEMALE, size, 0.0), 1.0e-9);
        assertEquals(0.9, ScaleRoll.roll(Sex.FEMALE, size, 1.0), 1.0e-9);
    }

    @Test
    void brokenJsonIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> BarnyardConfig.parse("{"));
    }

    @Test
    void pregnancyDefaultsToOneDayAndOneBabyForUnknownSpecies() {
        BarnyardConfig config = BarnyardConfig.parse("{}");

        assertTrue(config.pregnancyEnabled());
        assertEquals(new Range(24_000, 24_000), config.pregnancyRange("mod:unknown"));
        assertEquals(new Range(1, 1), config.litterRange("mod:unknown"));
    }

    @Test
    void speciesCanShortenPregnancyAndRaiseTheLitter() {
        BarnyardConfig config = BarnyardConfig.parse("""
                {
                  "species": {
                    "minecraft:chicken": { "pregnancyTicks": 12000, "litterSize": 3 }
                  }
                }
                """);

        assertEquals(new Range(12_000, 12_000), config.pregnancyRange("minecraft:chicken"));
        assertEquals(new Range(3, 3), config.litterRange("minecraft:chicken"));
        assertEquals(new Range(60_000, 72_000), config.pregnancyRange("minecraft:cow"));
    }

    @Test
    void legacyLitterSizeMeansAFixedLitter() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "litterSize": 3 }
                """);

        assertEquals(new Range(3, 3), config.litterRange("x:y"));
    }

    @Test
    void invertedRangesAreSwapped() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "species": { "minecraft:pig": { "litterMin": 5, "litterMax": 2 } } }
                """);

        assertEquals(new Range(2, 5), config.litterRange("minecraft:pig"));
    }

    @Test
    void outOfRangeRangesAreClamped() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "litterMin": -4, "litterMax": 40, "pregnancyTicksMin": -10, "pregnancyTicksMax": 9000000 }
                """);

        assertEquals(new Range(1, 8), config.litterRange("mod:unknown"));
        assertEquals(new Range(1, 480_000), config.pregnancyRange("mod:unknown"));
    }

    @Test
    void userValueBeatsPresetBeatsGlobal() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "species": { "minecraft:pig": { "litterMax": 6 } } }
                """);

        assertEquals(new Range(2, 6), config.litterRange("minecraft:pig"));
        assertEquals(new Range(1, 1), config.litterRange("minecraft:cow"));
        assertEquals(new Range(60_000, 72_000), config.pregnancyRange("minecraft:cow"));
        assertEquals(new Range(1, 1), config.litterRange("mod:unknown"));
        assertEquals(new Range(24_000, 24_000), config.pregnancyRange("mod:unknown"));
    }

    @Test
    void rangesRoundTripThroughTheConfigFileText() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "litterMin": 2, "litterMax": 3, "species": { "minecraft:pig": { "pregnancyTicksMin": 100, "pregnancyTicksMax": 200 } } }
                """);
        BarnyardConfig parsed = BarnyardConfig.parse(config.toJson());

        assertEquals(new Range(2, 3), parsed.litterRange("mod:unknown"));
        assertEquals(new Range(100, 200), parsed.pregnancyRange("minecraft:pig"));
    }

    @Test
    void pregnancyCanBeTurnedOffWithoutDisablingSexAndSize() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "pregnancyEnabled": false }
                """);

        assertFalse(config.pregnancyEnabled());
        assertTrue(config.speciesEnabled("minecraft:cow"));
    }

    @Test
    void agesDefaultThenGlobalThenSpecies() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "oldAfterTicks": 500000, "species": { "minecraft:rabbit": { "youngTicks": 12000, "deathAfterTicks": 0 } } }
                """);

        assertEquals(AgeSettings.DEFAULTS, BarnyardConfig.defaults().ageFor("minecraft:cow"));
        assertEquals(new AgeSettings(24_000L, 500_000L, 1_440_000L), config.ageFor("minecraft:cow"));
        assertEquals(new AgeSettings(12_000L, 500_000L, 0L), config.ageFor("minecraft:rabbit"));
    }

    @Test
    void badAgesAreClamped() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "youngTicks": -5, "deathAfterTicks": 999999999999, "species": { "minecraft:pig": { "oldAfterTicks": -1 } } }
                """);

        assertEquals(new AgeSettings(0L, 720_000L, 24_000_000L), config.ageFor("minecraft:cow"));
        assertEquals(new AgeSettings(0L, 0L, 24_000_000L), config.ageFor("minecraft:pig"));
    }

    @Test
    void agesRoundTripThroughTheConfigFileText() {
        BarnyardConfig config = BarnyardConfig.parse("""
                { "youngTicks": 100, "species": { "minecraft:pig": { "oldAfterTicks": 200, "deathAfterTicks": 300 } } }
                """);
        BarnyardConfig parsed = BarnyardConfig.parse(config.toJson());

        assertEquals(new AgeSettings(100L, 720_000L, 1_440_000L), parsed.ageFor("minecraft:cow"));
        assertEquals(new AgeSettings(100L, 200L, 300L), parsed.ageFor("minecraft:pig"));
    }

    @Test
    void defaultsRoundTripThroughTheConfigFileText() {
        String json = BarnyardConfig.defaults().toJson();
        BarnyardConfig parsed = BarnyardConfig.parse(json);

        assertTrue(json.contains("\"femaleScale\""));
        assertEquals(0.5, parsed.femaleRatio(), 1.0e-9);
        assertEquals(new SizeSettings(0.8, 1.2, 0.1), parsed.sizeFor("minecraft:cow"));
        assertTrue(parsed.speciesEnabled("minecraft:cow"));
    }

    @Test
    void bellyDefaults() {
        assertEquals(BellySettings.DEFAULTS, BarnyardConfig.parse("{}").bellyFor("minecraft:cow"));
    }

    @Test
    void bellyPerSpecies() {
        BarnyardConfig config = BarnyardConfig.parse("""
                {"bellyMaxWiden": 0.2, "bellyStart": 0.5, "species": {"minecraft:pig": {"bellyMaxWiden": 0.3}}}
                """);

        assertEquals(new BellySettings(0.3, 0.5), config.bellyFor("minecraft:pig"));
        assertEquals(new BellySettings(0.2, 0.5), config.bellyFor("minecraft:cow"));
    }

    @Test
    void bellyIsClamped() {
        assertEquals(new BellySettings(0.0, 0.99), BarnyardConfig.parse("{\"bellyMaxWiden\": -1, \"bellyStart\": 1.5}").bellyFor("minecraft:cow"));
        assertEquals(new BellySettings(1.0, 0.0), BarnyardConfig.parse("{\"bellyMaxWiden\": 5, \"bellyStart\": -1}").bellyFor("minecraft:cow"));
        assertEquals(new BellySettings(1.0, 0.0), BarnyardConfig.parse("""
                {"species": {"minecraft:cow": {"bellyMaxWiden": 5, "bellyStart": -1}}}
                """).bellyFor("minecraft:cow"));
    }

    @Test
    void bellyRoundTrips() {
        BarnyardConfig config = BarnyardConfig.parse("""
                {"bellyMaxWiden": 0.2, "species": {"minecraft:pig": {"bellyStart": 0.6}}}
                """);

        BarnyardConfig parsed = BarnyardConfig.parse(config.toJson());

        assertEquals(new BellySettings(0.2, 0.6), parsed.bellyFor("minecraft:pig"));
        assertEquals(new BellySettings(0.2, 0.4), parsed.bellyFor("minecraft:cow"));
    }
}
