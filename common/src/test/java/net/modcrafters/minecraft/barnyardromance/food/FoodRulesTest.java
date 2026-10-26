package net.modcrafters.minecraft.barnyardromance.food;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodRulesTest {
    private static final String COW = "minecraft:cow";
    private static final String WOLF = "minecraft:wolf";
    private static final String GRAZER_ID = "barnyard_romance:grazer";
    private static final String CARNIVORE_ID = "barnyard_romance:carnivore";
    private static final Diet GRAZER = new Diet(
            List.of(COW),
            List.of(new DietEntry(List.of(ItemMatch.parse("#c:crops")), 1.0), new DietEntry(List.of(ItemMatch.parse("#c:foods/fruit")), 0.75)),
            List.of(ItemMatch.parse("#barnyard_romance:contains_meat"))
    );
    private static final Diet CARNIVORE = new Diet(
            List.of(WOLF),
            List.of(new DietEntry(List.of(ItemMatch.parse("#barnyard_romance:contains_meat")), 1.0)),
            List.of()
    );

    @Test
    void refusedBeatsEverything() {
        FoodFile file = file(List.of(COW), List.of(), "minecraft:beef");
        Feed feed = FoodRules.resolve(COW, GRAZER_ID, GRAZER, List.of(file), item(Set.of("minecraft:beef", "#barnyard_romance:contains_meat"), false));

        assertInstanceOf(Feed.Refused.class, feed);
    }

    @Test
    void vanillaFoodIsNeverRefused() {
        Feed feed = FoodRules.resolve(COW, GRAZER_ID, GRAZER, List.of(), item(Set.of("#barnyard_romance:contains_meat"), true));

        assertEquals(new Feed.Eaten(FoodEffects.NONE, 1.0, false), feed);
    }

    @Test
    void foodFileBeatsDiet() {
        FoodFile file = file(List.of(COW), List.of(), "minecraft:apple");
        Feed feed = FoodRules.resolve(COW, GRAZER_ID, GRAZER, List.of(file), item(Set.of("minecraft:apple", "#c:foods/fruit"), false));

        assertEquals(new Feed.Eaten(file.effects(), 1.0, false), feed);
    }

    @Test
    void foodFileMatchesByDiet() {
        FoodFile file = file(List.of(), List.of(GRAZER_ID), "#c:crops");
        Feed feed = FoodRules.resolve(COW, GRAZER_ID, GRAZER, List.of(file), item(Set.of("#c:crops"), false));

        assertEquals(new Feed.Eaten(file.effects(), 1.0, false), feed);
    }

    @Test
    void foodFileForAnotherDietIsIgnored() {
        FoodFile file = file(List.of(), List.of(CARNIVORE_ID), "#c:crops");
        Feed feed = FoodRules.resolve(COW, GRAZER_ID, GRAZER, List.of(file), item(Set.of("#c:crops"), false));

        assertEquals(new Feed.Eaten(FoodEffects.NONE, 1.0, false), feed);
    }

    @Test
    void firstDietEntryWins() {
        Feed feed = FoodRules.resolve(COW, GRAZER_ID, GRAZER, List.of(), item(Set.of("#c:foods/fruit", "#c:crops"), false));

        assertEquals(new Feed.Eaten(FoodEffects.NONE, 1.0, false), feed);
    }

    @Test
    void laterDietEntryKeepsItsFactor() {
        Feed feed = FoodRules.resolve(COW, GRAZER_ID, GRAZER, List.of(), item(Set.of("#c:foods/fruit"), false));

        assertEquals(new Feed.Eaten(FoodEffects.NONE, 0.75, false), feed);
    }

    @Test
    void vanillaFoodBeatsALowDietFactor() {
        Feed feed = FoodRules.resolve(COW, GRAZER_ID, GRAZER, List.of(), item(Set.of("#c:foods/fruit"), true));

        assertEquals(new Feed.Eaten(FoodEffects.NONE, 1.0, false), feed);
    }

    @Test
    void vanillaFoodAlwaysFeeds() {
        Feed feed = FoodRules.resolve("othermod:yak", null, null, List.of(), item(Set.of(), true));

        assertEquals(new Feed.Eaten(FoodEffects.NONE, 1.0, false), feed);
    }

    @Test
    void missingDietFallsBackToVanillaFood() {
        Feed vanilla = FoodRules.resolve(COW, null, null, List.of(), item(Set.of("#c:crops"), true));
        Feed other = FoodRules.resolve(COW, null, null, List.of(), item(Set.of("#c:crops"), false));

        assertEquals(new Feed.Eaten(FoodEffects.NONE, 1.0, false), vanilla);
        assertInstanceOf(Feed.NotFood.class, other);
    }

    @Test
    void unknownItemIsNotFood() {
        Feed feed = FoodRules.resolve(COW, GRAZER_ID, GRAZER, List.of(), item(Set.of("minecraft:stone"), false));

        assertInstanceOf(Feed.NotFood.class, feed);
    }

    @Test
    void dietOverrideWins() {
        Map<String, Diet> diets = Map.of(GRAZER_ID, GRAZER, CARNIVORE_ID, CARNIVORE);

        assertEquals(CARNIVORE_ID, FoodRules.dietFor(COW, CARNIVORE_ID, diets).orElseThrow().getKey());
    }

    @Test
    void dietListsItsSpecies() {
        Map<String, Diet> diets = Map.of(GRAZER_ID, GRAZER, CARNIVORE_ID, CARNIVORE);

        assertEquals(GRAZER_ID, FoodRules.dietFor(COW, null, diets).orElseThrow().getKey());
        assertEquals(CARNIVORE_ID, FoodRules.dietFor(WOLF, null, diets).orElseThrow().getKey());
        assertTrue(FoodRules.dietFor("othermod:yak", null, diets).isEmpty());
    }

    @Test
    void firstListingDietBySortedIdWins() {
        String other = "apack:cows";
        Diet second = new Diet(List.of(COW), List.of(), List.of());
        Map<String, Diet> diets = Map.of(GRAZER_ID, GRAZER, other, second);

        assertEquals(other, FoodRules.dietFor(COW, null, diets).orElseThrow().getKey());
    }

    @Test
    void missingOverrideFallsBackToTheListingDiet() {
        Map<String, Diet> diets = Map.of(GRAZER_ID, GRAZER);

        assertEquals(GRAZER_ID, FoodRules.dietFor(COW, "apack:missing", diets).orElseThrow().getKey());
    }

    private static FoodFile file(List<String> species, List<String> diets, String items) {
        FoodEffects effects = new FoodEffects(Optional.of(0.1), Optional.of(3.0), Optional.empty(), 0.0, 0.0);
        return new FoodFile(species, diets, List.of(ItemMatch.parse(items)), false, effects);
    }

    // An item that is in the given tags and ids, written "#tag" or "item".
    private static ItemFacts item(Set<String> in, boolean vanillaFood) {
        return new ItemFacts() {
            @Override
            public boolean matches(ItemMatch match) {
                return in.contains((match.tag() ? "#" : "") + match.id());
            }

            @Override
            public boolean vanillaFood() {
                return vanillaFood;
            }

            @Override
            public OptionalInt nutrition() {
                return OptionalInt.empty();
            }
        };
    }
}
