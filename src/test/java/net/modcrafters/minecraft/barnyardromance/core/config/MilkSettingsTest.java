package net.modcrafters.minecraft.barnyardromance.core.config;

import net.modcrafters.minecraft.barnyardromance.BarnyardFeatures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MilkSettingsTest {
    @BeforeAll
    static void installFeatures() {
        BarnyardFeatures.ensureInstalled();
    }

    private static MilkEntry entry(String bucket, String result) {
        return new MilkEntry(bucket, result, null);
    }

    private static boolean holds(MilkSettings settings, String bucket, String result) {
        return settings.entries().contains(entry(bucket, result));
    }

    @Test
    void vanillaPresets() {
        BarnyardConfig config = BarnyardConfig.parse("{}");

        assertTrue(holds(config.milkFor("minecraft:cow"), "minecraft:bucket", "minecraft:milk_bucket"));
        assertTrue(holds(config.milkFor("minecraft:mooshroom"), "minecraft:bucket", "minecraft:milk_bucket"));
        assertTrue(holds(config.milkFor("minecraft:goat"), "minecraft:bucket", "barnyard_romance:goat_milk_bucket"));
        assertTrue(holds(config.milkFor("minecraft:sheep"), "minecraft:bucket", "barnyard_romance:sheep_milk_bucket"));
        assertFalse(config.milkFor("minecraft:cow").fromConfig());
    }

    @Test
    void meadowWoodenBucketOnVanilla() {
        BarnyardConfig config = BarnyardConfig.parse("{}");

        assertTrue(holds(config.milkFor("minecraft:cow"), "meadow:wooden_bucket", "meadow:wooden_milk_bucket"));
        assertTrue(holds(config.milkFor("minecraft:mooshroom"), "meadow:wooden_bucket", "meadow:wooden_milk_bucket"));
        assertTrue(holds(config.milkFor("minecraft:goat"), "meadow:wooden_bucket", "meadow:wooden_goat_milk_bucket"));
        assertTrue(holds(config.milkFor("minecraft:sheep"), "meadow:wooden_bucket", "meadow:wooden_sheep_milk_bucket"));
    }

    @Test
    void configListReplaces() {
        BarnyardConfig config = BarnyardConfig.parse(
                "{\"species\":{\"minecraft:cow\":{\"milk\":[{\"bucket\":\"minecraft:bowl\",\"result\":\"minecraft:mushroom_stew\"}]}}}");

        MilkSettings milk = config.milkFor("minecraft:cow");
        assertEquals(List.of(entry("minecraft:bowl", "minecraft:mushroom_stew")), milk.entries());
        assertTrue(milk.fromConfig());
    }

    @Test
    void badEntriesAreSkipped() {
        BarnyardConfig config = BarnyardConfig.parse("{\"species\":{\"minecraft:cow\":{\"milk\":["
                + "{\"result\":\"minecraft:milk_bucket\"},"
                + "{\"bucket\":\"minecraft:bucket\"},"
                + "{\"bucket\":5,\"result\":\"minecraft:milk_bucket\"},"
                + "{\"bucket\":\"minecraft:bucket\",\"result\":true},"
                + "\"nonsense\","
                + "{\"bucket\":\"minecraft:glass_bottle\",\"result\":\"minecraft:honey_bottle\",\"sound\":\"minecraft:entity.cow.milk\"}"
                + "]}}}");

        assertEquals(List.of(new MilkEntry("minecraft:glass_bottle", "minecraft:honey_bottle", "minecraft:entity.cow.milk")),
                config.milkFor("minecraft:cow").entries());
    }

    @Test
    void milkingItemsCoverPresetsAndConfig() {
        BarnyardConfig config = BarnyardConfig.parse(
                "{\"species\":{\"minecraft:pig\":{\"milk\":[{\"bucket\":\"minecraft:bowl\",\"result\":\"minecraft:mushroom_stew\"}]}}}");

        assertTrue(config.milkingItems().contains("minecraft:bucket"));
        assertTrue(config.milkingItems().contains("meadow:wooden_bucket"));
        assertTrue(config.milkingItems().contains("minecraft:bowl"));
        assertFalse(config.milkingItems().contains("minecraft:glass_bottle"));
    }

    @Test
    void a_tag_entry_parses_and_round_trips() {
        String json = "{\"species\":{\"minecraft:sheep\":{\"milk\":["
                + "{\"bucket\":\"#barnyard_romance:pails\",\"result\":\"barnyard_romance:sheep_milk_bucket\"}]}}}";

        BarnyardConfig config = BarnyardConfig.parse(json);
        BarnyardConfig again = BarnyardConfig.parse(config.toJson());

        assertEquals(List.of(entry("#barnyard_romance:pails", "barnyard_romance:sheep_milk_bucket")), config.milkFor("minecraft:sheep").entries());
        assertEquals(config.milkFor("minecraft:sheep"), again.milkFor("minecraft:sheep"));
    }

    @Test
    void milkingItems_includes_tag_entries() {
        BarnyardConfig config = BarnyardConfig.parse(
                "{\"species\":{\"minecraft:pig\":{\"milk\":[{\"bucket\":\"#c:tools/bowls\",\"result\":\"minecraft:mushroom_stew\"}]}}}");

        assertTrue(config.milkingItems().contains("#c:tools/bowls"));
        // The wooly cow's built-in pail entry.
        assertTrue(config.milkingItems().contains("#barnyard_romance:pails"));
    }

    @Test
    void woolyCowsTakePailsAsCowMilk() {
        MilkSettings milk = BarnyardConfig.parse("{}").milkFor("meadow:wooly_cow");

        assertTrue(holds(milk, "meadow:wooden_bucket", "meadow:wooden_milk_bucket"));
        assertTrue(holds(milk, "#barnyard_romance:pails", "minecraft:milk_bucket"));
        assertFalse(milk.entries().stream().anyMatch(entry -> entry.bucket().equals("minecraft:bucket")));
    }

    @Test
    void pigsGiveNoMilk() {
        assertTrue(BarnyardConfig.parse("{}").milkFor("minecraft:pig").entries().isEmpty());
    }

    @Test
    void aDisabledSpeciesGivesNoMilk() {
        BarnyardConfig config = BarnyardConfig.parse("{\"species\":{\"minecraft:cow\":{\"enabled\":false}}}");

        assertTrue(config.milkFor("minecraft:cow").entries().isEmpty());
    }

    @Test
    void toJsonRoundTrips() {
        String json = "{\"species\":{\"minecraft:cow\":{\"milk\":[{\"bucket\":\"minecraft:bowl\",\"result\":\"minecraft:mushroom_stew\",\"sound\":\"minecraft:entity.cow.milk\"}]}}}";

        BarnyardConfig again = BarnyardConfig.parse(BarnyardConfig.parse(json).toJson());

        assertEquals(BarnyardConfig.parse(json).milkFor("minecraft:cow"), again.milkFor("minecraft:cow"));
        assertTrue(again.milkFor("minecraft:cow").fromConfig());
    }
}
