package net.modcrafters.minecraft.barnyardromance.food;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoodScanTest {
    private static final String SWEETS = "test:sweets";
    private static final String MEAT = "test:meat";
    private static final Predicate<String> ALL_FOOD = item -> true;

    @Test
    void sugarCarriesIntoCake() {
        FoodScan.ScanResult result = FoodScan.run(
                List.of(recipe("cake", Set.of("sugar"), Set.of("milk_bucket"), Set.of("egg"), Set.of("wheat"))),
                Map.of(SWEETS, Set.of("sugar")),
                ALL_FOOD
        );

        assertEquals(sorted("cake"), result.added().get(SWEETS));
        assertEquals("test:cake_recipe", result.because().get("cake"));
    }

    @Test
    void deepChainsPropagate() {
        FoodScan.ScanResult result = FoodScan.run(
                List.of(recipe("pie", Set.of("dough"), Set.of("pumpkin")), recipe("dough", Set.of("sugar"), Set.of("wheat"))),
                Map.of(SWEETS, Set.of("sugar")),
                item -> !item.equals("dough")
        );

        assertEquals(sorted("pie"), result.added().get(SWEETS));
    }

    @Test
    void everyOptionMustBeInTheGroup() {
        FoodScan.ScanResult result = FoodScan.run(
                List.of(recipe("stew", Set.of("beef", "carrot")), recipe("sausage", Set.of("beef", "porkchop"))),
                Map.of(MEAT, Set.of("beef", "porkchop")),
                ALL_FOOD
        );

        assertEquals(sorted("sausage"), result.added().get(MEAT));
    }

    @Test
    void anyRecipeIsEnough() {
        FoodScan.ScanResult result = FoodScan.run(
                List.of(recipe("bread", Set.of("wheat")), recipe("bread", Set.of("wheat"), Set.of("sugar"))),
                Map.of(SWEETS, Set.of("sugar")),
                ALL_FOOD
        );

        assertEquals(sorted("bread"), result.added().get(SWEETS));
    }

    @Test
    void onlyFoodIsWritten() {
        FoodScan.ScanResult result = FoodScan.run(
                List.of(recipe("fermented_spider_eye", Set.of("sugar"), Set.of("spider_eye"))),
                Map.of(SWEETS, Set.of("sugar")),
                item -> !item.equals("fermented_spider_eye")
        );

        assertEquals(sorted(), result.added().get(SWEETS));
    }

    @Test
    void seedsAreNotRepeated() {
        FoodScan.ScanResult result = FoodScan.run(
                List.of(recipe("cake", Set.of("sugar")), recipe("sugar", Set.of("honey_bottle"))),
                Map.of(SWEETS, Set.of("sugar", "cake", "honey_bottle")),
                ALL_FOOD
        );

        assertEquals(sorted(), result.added().get(SWEETS));
    }

    @Test
    void cyclesStop() {
        FoodScan.ScanResult result = FoodScan.run(
                List.of(recipe("hay_block", Set.of("wheat")), recipe("wheat", Set.of("hay_block")), recipe("bread", Set.of("wheat"))),
                Map.of(SWEETS, Set.of("hay_block")),
                ALL_FOOD
        );

        assertEquals(sorted("bread", "wheat"), result.added().get(SWEETS));
    }

    @Test
    void emptyIngredientsCarryNothing() {
        FoodScan.ScanResult result = FoodScan.run(
                List.of(recipe("bowl_of_air", Set.of())),
                Map.of(SWEETS, Set.of("sugar")),
                ALL_FOOD
        );

        assertEquals(sorted(), result.added().get(SWEETS));
    }

    @SafeVarargs
    private static FoodScan.ScanRecipe recipe(String result, Set<String>... ingredients) {
        return new FoodScan.ScanRecipe("test:" + result + "_recipe", "minecraft:crafting", result, List.of(ingredients));
    }

    private static SortedSet<String> sorted(String... items) {
        return new TreeSet<>(List.of(items));
    }
}
