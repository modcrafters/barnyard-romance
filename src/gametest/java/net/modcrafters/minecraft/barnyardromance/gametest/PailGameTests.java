package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.modcrafters.minecraft.barnyardromance.milk.MilkItems;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailContents;
import net.modcrafters.minecraft.barnyardromance.pail.PailFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailItems;

import java.util.List;
import java.util.Optional;

// In-game checks of the pail items: what they hold, their tag and how they are crafted, and the milk fluids they carry.
public final class PailGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("pails_have_their_capacities", 20, PailGameTests::pailsHaveTheirCapacities),
            new BreedingGameTests.Case("a_new_pail_is_empty", 20, PailGameTests::aNewPailIsEmpty),
            new BreedingGameTests.Case("pails_are_in_the_pails_tag", 20, PailGameTests::pailsAreInThePailsTag),
            new BreedingGameTests.Case("pail_recipes_craft", 20, PailGameTests::pailRecipesCraft),
            new BreedingGameTests.Case("the_bowl_and_bucket_still_craft", 20, PailGameTests::theBowlAndBucketStillCraft),
            new BreedingGameTests.Case("milk_buckets_map_to_their_fluids", 20, PailGameTests::milkBucketsMapToTheirFluids),
            new BreedingGameTests.Case("fluids_map_back_to_their_buckets", 20, PailGameTests::fluidsMapBackToTheirBuckets),
            new BreedingGameTests.Case("pail_fluids_are_milk_only", 20, PailGameTests::pailFluidsAreMilkOnly),
            new BreedingGameTests.Case("our_milks_are_in_c_milk", 20, PailGameTests::ourMilksAreInCMilk),
            new BreedingGameTests.Case("milks_have_names_and_colours", 20, PailGameTests::milksHaveNamesAndColours),
            new BreedingGameTests.Case("an_unknown_fluid_names_itself_by_id", 20, PailGameTests::anUnknownFluidNamesItselfById)
    );
    private static final TagKey<Fluid> C_MILK = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", "milk"));

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

    public static void milkBucketsMapToTheirFluids(GameTestHelper helper) {
        helper.assertTrue(PailFluids.forBucket(Items.MILK_BUCKET).equals(Optional.of(MilkFluids.COW.value())),
                "the milk bucket holds " + PailFluids.forBucket(Items.MILK_BUCKET));
        helper.assertTrue(PailFluids.forBucket(MilkItems.GOAT_MILK_BUCKET).equals(Optional.of(MilkFluids.GOAT.value())),
                "the goat milk bucket holds " + PailFluids.forBucket(MilkItems.GOAT_MILK_BUCKET));
        helper.assertTrue(PailFluids.forBucket(MilkItems.SHEEP_MILK_BUCKET).equals(Optional.of(MilkFluids.SHEEP.value())),
                "the sheep milk bucket holds " + PailFluids.forBucket(MilkItems.SHEEP_MILK_BUCKET));
        // Water isn't a pail fluid, and an empty bucket holds nothing.
        helper.assertTrue(PailFluids.forBucket(Items.WATER_BUCKET).isEmpty(), "the water bucket holds " + PailFluids.forBucket(Items.WATER_BUCKET));
        helper.assertTrue(PailFluids.forBucket(Items.BUCKET).isEmpty(), "the empty bucket holds " + PailFluids.forBucket(Items.BUCKET));
        helper.succeed();
    }

    public static void fluidsMapBackToTheirBuckets(GameTestHelper helper) {
        helper.assertTrue(PailFluids.bucketFor(MilkFluids.COW.value()).equals(Optional.of(Items.MILK_BUCKET)),
                "cow milk fills " + PailFluids.bucketFor(MilkFluids.COW.value()));
        helper.assertTrue(PailFluids.bucketFor(MilkFluids.GOAT.value()).equals(Optional.of(MilkItems.GOAT_MILK_BUCKET)),
                "goat milk fills " + PailFluids.bucketFor(MilkFluids.GOAT.value()));
        helper.assertTrue(PailFluids.bucketFor(MilkFluids.SHEEP.value()).equals(Optional.of(MilkItems.SHEEP_MILK_BUCKET)),
                "sheep milk fills " + PailFluids.bucketFor(MilkFluids.SHEEP.value()));
        helper.assertTrue(PailFluids.bucketFor(Fluids.EMPTY).isEmpty(), "no fluid fills " + PailFluids.bucketFor(Fluids.EMPTY));
        helper.succeed();
    }

    public static void pailFluidsAreMilkOnly(GameTestHelper helper) {
        helper.assertTrue(!PailFluids.accepts(Fluids.WATER), "a pail takes water");
        helper.assertTrue(!PailFluids.accepts(Fluids.LAVA), "a pail takes lava");
        helper.assertTrue(!PailFluids.accepts(Fluids.EMPTY), "a pail takes the empty fluid");
        for (Holder<Fluid> milk : List.of(MilkFluids.COW, MilkFluids.GOAT, MilkFluids.SHEEP)) {
            helper.assertTrue(PailFluids.accepts(milk.value()), "a pail doesn't take " + milk.getRegisteredName());
        }
        helper.succeed();
    }

    public static void ourMilksAreInCMilk(GameTestHelper helper) {
        for (Holder<Fluid> milk : List.of(MilkFluids.COW, MilkFluids.GOAT, MilkFluids.SHEEP)) {
            helper.assertTrue(milk.is(C_MILK), milk.getRegisteredName() + " isn't in c:milk");
        }
        helper.succeed();
    }

    public static void milksHaveNamesAndColours(GameTestHelper helper) {
        helper.assertTrue(translationKey(PailFluids.name(MilkFluids.GOAT_ID)).equals("fluid.barnyard_romance.goat_milk"),
                "goat milk is called " + PailFluids.name(MilkFluids.GOAT_ID));
        helper.assertTrue(translationKey(PailFluids.name(MilkFluids.SHEEP_ID)).equals("fluid.barnyard_romance.sheep_milk"),
                "sheep milk is called " + PailFluids.name(MilkFluids.SHEEP_ID));
        // On NeoForge cow milk is NeoForge's own, with NeoForge's key, so all both loaders share is that it's translated.
        helper.assertTrue(!translationKey(PailFluids.name(MilkFluids.COW_ID)).isEmpty(), "cow milk is called " + PailFluids.name(MilkFluids.COW_ID));
        helper.assertTrue(PailFluids.colour(MilkFluids.COW_ID) == 0xFFFFFF, "cow milk is " + Integer.toHexString(PailFluids.colour(MilkFluids.COW_ID)));
        helper.assertTrue(PailFluids.colour(MilkFluids.GOAT_ID) == 0xFFF8E7, "goat milk is " + Integer.toHexString(PailFluids.colour(MilkFluids.GOAT_ID)));
        helper.assertTrue(PailFluids.colour(MilkFluids.SHEEP_ID) == 0xFDF5E2, "sheep milk is " + Integer.toHexString(PailFluids.colour(MilkFluids.SHEEP_ID)));
        helper.succeed();
    }

    public static void anUnknownFluidNamesItselfById(GameTestHelper helper) {
        Component name = PailFluids.name(Identifier.parse("gone:whey"));
        helper.assertTrue(name.getString().contains("gone:whey"), "a fluid that's gone is called " + name.getString());
        helper.assertTrue(PailFluids.colour(Identifier.parse("gone:whey")) == 0xFFFFFF, "a fluid that's gone has a colour");
        helper.succeed();
    }

    // The key of a translated name, or "" for a name written out, such as a fluid's id.
    private static String translationKey(Component name) {
        return name.getContents() instanceof TranslatableContents translatable ? translatable.getKey() : "";
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
