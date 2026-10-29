package net.modcrafters.minecraft.barnyardromance.food;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodJsonTest {
    @Test
    void parsesTagsAndItems() {
        assertEquals(new ItemMatch(true, "c:crops"), ItemMatch.parse("#c:crops"));
        assertEquals(new ItemMatch(false, "minecraft:hay_block"), ItemMatch.parse("minecraft:hay_block"));
        assertEquals(new ItemMatch(false, "minecraft:wheat"), ItemMatch.parse("wheat"));
        assertEquals(new ItemMatch(true, "c:foods/raw_meat"), ItemMatch.parse("#c:foods/raw_meat"));
    }

    @Test
    void oneStringOrAList() {
        Diet diet = FoodJson.diet("""
                { "species": ["minecraft:cow"],
                  "eats": [ { "items": "#c:crops" }, { "items": ["minecraft:hay_block", "#c:seeds"], "factor": 0.5 } ],
                  "refuses": "#barnyard_romance:contains_meat" }
                """);

        assertEquals(List.of("minecraft:cow"), diet.species());
        assertEquals(List.of(ItemMatch.parse("#c:crops")), diet.eats().get(0).items());
        assertEquals(1.0, diet.eats().get(0).factor(), 1.0e-9);
        assertEquals(List.of(ItemMatch.parse("minecraft:hay_block"), ItemMatch.parse("#c:seeds")), diet.eats().get(1).items());
        assertEquals(0.5, diet.eats().get(1).factor(), 1.0e-9);
        assertEquals(List.of(ItemMatch.parse("#barnyard_romance:contains_meat")), diet.refuses());
    }

    @Test
    void clampsFactorsAndEffects() {
        Diet diet = FoodJson.diet("""
                { "eats": [ { "items": "wheat", "factor": 9 }, { "items": "apple", "factor": -1 } ] }
                """);
        FoodFile file = FoodJson.foodFile("""
                { "diets": ["barnyard_romance:grazer"], "items": "wheat",
                  "effects": { "familiarity": 3, "heal": 5000, "pregnancySpeed": { "factor": 50, "ticks": 9000000 },
                               "pregnancyProgress": -4, "babyImprove": 2 } }
                """);

        assertEquals(4.0, diet.eats().get(0).factor(), 1.0e-9);
        assertEquals(0.0, diet.eats().get(1).factor(), 1.0e-9);
        FoodEffects effects = file.effects();
        assertEquals(1.0, effects.familiarity().orElseThrow(), 1.0e-9);
        assertEquals(1024.0, effects.heal().orElseThrow(), 1.0e-9);
        assertEquals(10.0, effects.pregnancySpeed().orElseThrow().factor(), 1.0e-9);
        assertEquals(480_000L, effects.pregnancySpeed().orElseThrow().ticks());
        assertEquals(-1.0, effects.pregnancyProgress(), 1.0e-9);
        assertEquals(1.0, effects.babyImprove(), 1.0e-9);
        assertTrue(effects.needsPregnancy());
    }

    @Test
    void missingEffectsAreEmpty() {
        FoodFile file = FoodJson.foodFile("""
                { "species": ["minecraft:horse"], "items": "wheat", "effects": { "heal": 2 } }
                """);

        assertFalse(file.superfood());
        assertTrue(file.effects().familiarity().isEmpty());
        assertTrue(file.effects().pregnancySpeed().isEmpty());
        assertFalse(file.effects().needsPregnancy());
    }

    @Test
    void foodFileNeedsSpeciesOrDiet() {
        assertThrows(IllegalArgumentException.class, () -> FoodJson.foodFile("""
                { "items": "wheat", "effects": { "heal": 2 } }
                """));
    }

    @Test
    void foodFileNeedsItems() {
        assertThrows(IllegalArgumentException.class, () -> FoodJson.foodFile("""
                { "species": ["minecraft:horse"] }
                """));
    }

    @Test
    void rejectsBadIds() {
        assertThrows(IllegalArgumentException.class, () -> ItemMatch.parse("#Bad Tag"));
        assertThrows(IllegalArgumentException.class, () -> FoodJson.diet("""
                { "eats": [ { "items": "not an id!" } ] }
                """));
        assertThrows(IllegalArgumentException.class, () -> FoodJson.diet("""
                { "species": ["Minecraft:Cow"] }
                """));
    }

    @Test
    void rejectsBrokenJson() {
        assertThrows(IllegalArgumentException.class, () -> FoodJson.diet("{"));
        assertThrows(IllegalArgumentException.class, () -> FoodJson.diet("[]"));
        assertThrows(IllegalArgumentException.class, () -> FoodJson.foodFile("""
                { "species": ["minecraft:horse"], "items": "wheat", "effects": { "heal": "lots" } }
                """));
    }

    @Test
    void startsMilkIsAnEffect() {
        FoodFile file = FoodJson.foodFile("""
                { "diets": ["barnyard_romance:grazer"], "items": "wheat", "superfood": true, "effects": { "startsMilk": true } }
                """);

        assertTrue(file.effects().startsMilk());
        assertFalse(file.effects().needsPregnancy());
        assertFalse(FoodJson.foodFile("""
                { "species": ["minecraft:horse"], "items": "wheat", "effects": { "heal": 2 } }
                """).effects().startsMilk());
    }
}
