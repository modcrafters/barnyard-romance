package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.modcrafters.minecraft.barnyardromance.pail.PailContents;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailItems;

import java.util.List;
import java.util.Optional;

// In-game checks of the pail items: what they hold, their tag and how they are crafted.
public final class PailGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("pails_have_their_capacities", 20, PailGameTests::pailsHaveTheirCapacities),
            new BreedingGameTests.Case("a_new_pail_is_empty", 20, PailGameTests::aNewPailIsEmpty),
            new BreedingGameTests.Case("pails_are_in_the_pails_tag", 20, PailGameTests::pailsAreInThePailsTag),
            new BreedingGameTests.Case("pail_recipes_craft", 20, PailGameTests::pailRecipesCraft),
            new BreedingGameTests.Case("the_bowl_and_bucket_still_craft", 20, PailGameTests::theBowlAndBucketStillCraft)
    );

    private PailGameTests() {
    }

    public static void pailsHaveTheirCapacities(GameTestHelper helper) {
        helper.assertTrue(((PailItem) PailItems.WOODEN_PAIL).capacity() == 2000, "the wooden pail holds " + ((PailItem) PailItems.WOODEN_PAIL).capacity());
        helper.assertTrue(((PailItem) PailItems.COPPER_PAIL).capacity() == 3000, "the copper pail holds " + ((PailItem) PailItems.COPPER_PAIL).capacity());
        helper.assertTrue(((PailItem) PailItems.IRON_PAIL).capacity() == 5000, "the iron pail holds " + ((PailItem) PailItems.IRON_PAIL).capacity());
        helper.succeed();
    }

    public static void aNewPailIsEmpty(GameTestHelper helper) {
        for (Item pail : List.of(PailItems.WOODEN_PAIL, PailItems.COPPER_PAIL, PailItems.IRON_PAIL)) {
            helper.assertTrue(PailItem.contents(new ItemStack(pail)).equals(PailContents.EMPTY), "a new pail isn't empty: " + pail);
        }
        ItemStack pail = new ItemStack(PailItems.IRON_PAIL);
        PailItem.setContents(pail, new PailContents(Identifier.fromNamespaceAndPath("barnyard_romance", "goat_milk"), 1000));
        helper.assertTrue(PailItem.contents(pail).amount() == 1000, "the pail kept " + PailItem.contents(pail));
        PailItem.setContents(pail, PailContents.EMPTY);
        helper.assertTrue(PailItem.contents(pail).equals(PailContents.EMPTY), "an emptied pail holds " + PailItem.contents(pail));
        helper.succeed();
    }

    public static void pailsAreInThePailsTag(GameTestHelper helper) {
        for (Item pail : List.of(PailItems.WOODEN_PAIL, PailItems.COPPER_PAIL, PailItems.IRON_PAIL)) {
            helper.assertTrue(new ItemStack(pail).is(PailItems.PAILS), pail + " isn't in the pails tag");
        }
        helper.succeed();
    }

    public static void pailRecipesCraft(GameTestHelper helper) {
        helper.assertTrue(craft(helper, Items.OAK_PLANKS).is(PailItems.WOODEN_PAIL), "planks make " + craft(helper, Items.OAK_PLANKS));
        helper.assertTrue(craft(helper, Items.COPPER_INGOT).is(PailItems.COPPER_PAIL), "copper makes " + craft(helper, Items.COPPER_INGOT));
        helper.assertTrue(craft(helper, Items.IRON_INGOT).is(PailItems.IRON_PAIL), "iron makes " + craft(helper, Items.IRON_INGOT));
        helper.succeed();
    }

    public static void theBowlAndBucketStillCraft(GameTestHelper helper) {
        helper.assertTrue(craftV(helper, Items.OAK_PLANKS).is(Items.BOWL), "three planks in a V make " + craftV(helper, Items.OAK_PLANKS));
        helper.assertTrue(craftV(helper, Items.IRON_INGOT).is(Items.BUCKET), "three iron in a V make " + craftV(helper, Items.IRON_INGOT));
        helper.succeed();
    }

    // A stick above a V of the material.
    private static ItemStack craft(GameTestHelper helper, Item material) {
        return result(helper, List.of(
                ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY,
                new ItemStack(material), ItemStack.EMPTY, new ItemStack(material),
                ItemStack.EMPTY, new ItemStack(material), ItemStack.EMPTY));
    }

    // Three of the material in a V, which vanilla's bowl and bucket use.
    private static ItemStack craftV(GameTestHelper helper, Item material) {
        return result(helper, List.of(
                new ItemStack(material), ItemStack.EMPTY, new ItemStack(material),
                ItemStack.EMPTY, new ItemStack(material), ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY));
    }

    private static ItemStack result(GameTestHelper helper, List<ItemStack> grid) {
        CraftingInput input = CraftingInput.of(3, 3, grid);
        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        if (recipe.isEmpty()) {
            return ItemStack.EMPTY;
        }
        //? if >=1.21.2 {
        return recipe.get().value().assemble(input);
        //?} else
        //return recipe.get().value().assemble(input, helper.getLevel().registryAccess());
    }
}
