package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.food.FoodData;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;

// In-game checks for diets, food files and superfoods, run by each loader's game test server.
public final class FoodGameTests {
    private static final TagKey<Item> CONTAINS_MEAT = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "contains_meat"));

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("built_in_diets_load", 20, FoodGameTests::builtInDietsLoad),
            new BreedingGameTests.Case("group_tag_holds_vanilla_meat", 20, FoodGameTests::groupTagHoldsVanillaMeat)
    );

    private FoodGameTests() {
    }

    public static void builtInDietsLoad(GameTestHelper helper) {
        String cow = FoodData.dietFor("minecraft:cow").map(Map.Entry::getKey).orElse("none");
        String wolf = FoodData.dietFor("minecraft:wolf").map(Map.Entry::getKey).orElse("none");
        helper.assertTrue(cow.equals("barnyard_romance:grazer"), "a cow eats as " + cow);
        helper.assertTrue(wolf.equals("barnyard_romance:carnivore"), "a wolf eats as " + wolf);
        helper.succeed();
    }

    public static void groupTagHoldsVanillaMeat(GameTestHelper helper) {
        for (Item item : List.of(Items.RABBIT_STEW, Items.BEEF, Items.COD)) {
            helper.assertTrue(new ItemStack(item).is(CONTAINS_MEAT), item + " is not in contains_meat");
        }
        helper.assertTrue(!new ItemStack(Items.WHEAT).is(CONTAINS_MEAT), "wheat is in contains_meat");
        helper.succeed();
    }
}
