package net.modcrafters.minecraft.barnyardromance.config;

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
    void pregnancyDefaultsToOneDayAndOneBaby() {
        BarnyardConfig config = BarnyardConfig.parse("{}");

        assertTrue(config.pregnancyEnabled());
        assertEquals(24_000, config.pregnancyTicks("minecraft:cow"));
        assertEquals(1, config.litterSize("minecraft:cow"));
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

        assertEquals(12_000, config.pregnancyTicks("minecraft:chicken"));
        assertEquals(3, config.litterSize("minecraft:chicken"));
        assertEquals(24_000, config.pregnancyTicks("minecraft:cow"));
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
    void defaultsRoundTripThroughTheConfigFileText() {
        String json = BarnyardConfig.defaults().toJson();
        BarnyardConfig parsed = BarnyardConfig.parse(json);

        assertTrue(json.contains("\"femaleScale\""));
        assertEquals(0.5, parsed.femaleRatio(), 1.0e-9);
        assertEquals(new SizeSettings(0.8, 1.2, 0.1), parsed.sizeFor("minecraft:cow"));
        assertTrue(parsed.speciesEnabled("minecraft:cow"));
    }
}
