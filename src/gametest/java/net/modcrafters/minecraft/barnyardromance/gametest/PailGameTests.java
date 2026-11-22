package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.modcrafters.minecraft.barnyardromance.api.event.Milking;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.Refusable;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.modcrafters.minecraft.barnyardromance.milk.MilkItems;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailContents;
import net.modcrafters.minecraft.barnyardromance.pail.PailFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailItems;
import net.modcrafters.minecraft.barnyardromance.pail.PailMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;
import static net.modcrafters.minecraft.barnyardromance.milk.MilkSlices.LACTATION;

// In-game checks of the pail items: what they hold, their tag and how they are crafted, the milk fluids they carry,
// milking into them, and their window.
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
            new BreedingGameTests.Case("an_unknown_fluid_names_itself_by_id", 20, PailGameTests::anUnknownFluidNamesItselfById),
            new BreedingGameTests.Case("a_pail_takes_a_cows_milk", 20, PailGameTests::aPailTakesACowsMilk),
            new BreedingGameTests.Case("a_second_milking_adds_to_the_pail", 20, PailGameTests::aSecondMilkingAddsToThePail),
            new BreedingGameTests.Case("a_full_pail_spends_nothing", 20, PailGameTests::aFullPailSpendsNothing),
            new BreedingGameTests.Case("a_pail_of_goat_milk_refuses_a_cow", 20, PailGameTests::aPailOfGoatMilkRefusesACow),
            new BreedingGameTests.Case("a_pail_obeys_the_milking_rules", 20, PailGameTests::aPailObeysTheMilkingRules),
            new BreedingGameTests.Case("a_cancelled_milking_leaves_the_pail", 20, PailGameTests::aCancelledMilkingLeavesThePail),
            new BreedingGameTests.Case("a_listener_result_without_a_fluid_milks_nothing", 20, PailGameTests::aListenerResultWithoutAFluidMilksNothing),
            new BreedingGameTests.Case("a_creative_player_fills_the_pail", 20, PailGameTests::aCreativePlayerFillsThePail),
            new BreedingGameTests.Case("an_offhand_pail_fills", 20, PailGameTests::anOffhandPailFills),
            new BreedingGameTests.Case("a_config_entry_naming_a_pail_wins", 20, PailGameTests::aConfigEntryNamingAPailWins),
            new BreedingGameTests.Case("using_a_pail_opens_its_window", 20, PailGameTests::usingAPailOpensItsWindow),
            new BreedingGameTests.Case("an_empty_bucket_takes_a_thousand", 20, PailGameTests::anEmptyBucketTakesAThousand),
            new BreedingGameTests.Case("a_milk_bucket_pours_in", 20, PailGameTests::aMilkBucketPoursIn),
            new BreedingGameTests.Case("a_different_milk_bucket_is_refused", 20, PailGameTests::aDifferentMilkBucketIsRefused),
            new BreedingGameTests.Case("a_bucket_needs_a_full_thousand", 20, PailGameTests::aBucketNeedsAFullThousand),
            new BreedingGameTests.Case("pour_out_empties_the_pail", 20, PailGameTests::pourOutEmptiesThePail),
            new BreedingGameTests.Case("closing_returns_the_slot_item", 20, PailGameTests::closingReturnsTheSlotItem),
            new BreedingGameTests.Case("shift_clicking_sixteen_buckets_moves_one", 20, PailGameTests::shiftClickingSixteenBucketsMovesOne),
            new BreedingGameTests.Case("moving_the_pail_closes_the_menu", 20, PailGameTests::movingThePailClosesTheMenu),
            new BreedingGameTests.Case("a_dropped_fluid_can_still_be_poured_out", 20, PailGameTests::aDroppedFluidCanStillBePouredOut)
    );
    // Familiar enough to be milked but under refusalBelow, with every roll a refusal: a milking that gets as far as the
    // roll is refused, so one that isn't refused never rolled.
    private static final String ALWAYS_REFUSES = "{\"tuning\": {\"refusalBelow\": 0.5, \"refusalChance\": 1.0, \"familiarityThreshold\": 0.3}}";
    private static final double BELOW_REFUSAL = 0.45;
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

    public static void aPailTakesACowsMilk(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        Lactation before = BreedingGameTests.traits(helper, cow).get(LACTATION);
        ItemStack pail = new ItemStack(PailItems.IRON_PAIL);
        Player player = holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());

        GameTestCompat.interact(cow, player);

        assertHolds(helper, player.getMainHandItem(), pail, new PailContents(MilkFluids.COW_ID, 1000));
        helper.assertTrue(!BreedingGameTests.traits(helper, cow).get(LACTATION).equals(before), "the cow wasn't counted as milked");
        helper.succeed();
    }

    public static void aSecondMilkingAddsToThePail(GameTestHelper helper) {
        ItemStack pail = new ItemStack(PailItems.IRON_PAIL);
        Player player = holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());

        GameTestCompat.interact(ProductGameTests.mother(helper, EntityTypes.COW, 0.5), player);
        GameTestCompat.interact(ProductGameTests.mother(helper, EntityTypes.COW, 0.5), player);

        assertHolds(helper, player.getMainHandItem(), pail, new PailContents(MilkFluids.COW_ID, 2000));
        helper.succeed();
    }

    public static void aFullPailSpendsNothing(GameTestHelper helper) {
        assertSpendsNothing(helper, pail(PailItems.WOODEN_PAIL, MilkFluids.COW_ID, 2000), "message.barnyard_romance.pail.full");
    }

    public static void aPailOfGoatMilkRefusesACow(GameTestHelper helper) {
        assertSpendsNothing(helper, pail(PailItems.IRON_PAIL, MilkFluids.GOAT_ID, 1000), "message.barnyard_romance.pail.holds");
    }

    // The pail is turned away before any rule runs: the player is told why, and the cow keeps her milking, her
    // familiarity and her chance to refuse. A bucket on the same cow afterwards is refused, so the roll was armed.
    private static void assertSpendsNothing(GameTestHelper helper, ItemStack pail, String message) {
        UpkeepGameTests.withConfig(ALWAYS_REFUSES, () -> {
            Animal cow = FoodGameTests.withFamiliarity(helper, ProductGameTests.mother(helper, EntityTypes.COW, 0.5), BELOW_REFUSAL);
            Lactation before = BreedingGameTests.traits(helper, cow).get(LACTATION);
            PailContents contents = PailItem.contents(pail);
            List<Component> messages = new ArrayList<>();
            Player player = holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, messages);
            long today = GameDay.of(helper.getLevel().getOverworldClockTime());

            InteractionResult result = GameTestCompat.interact(cow, player);

            helper.assertTrue(result == InteractionResult.CONSUME, "the pail on the cow answered " + result);
            assertHolds(helper, player.getMainHandItem(), pail, contents);
            helper.assertTrue(messages.size() == 1 && translationKey(messages.get(0)).equals(message), "the player was told " + messages);
            helper.assertTrue(BreedingGameTests.traits(helper, cow).get(LACTATION).equals(before), "the cow was counted as milked");
            FoodGameTests.assertFamiliarity(helper, cow, BELOW_REFUSAL);
            helper.assertTrue(!BreedingGameTests.traits(helper, cow).get(CARE).refusals().refused(Refusable.MILKING, today), "the cow refused the pail");

            GameTestCompat.interact(cow, FoodGameTests.holding(helper, Items.BUCKET));
            helper.assertTrue(BreedingGameTests.traits(helper, cow).get(CARE).refusals().refused(Refusable.MILKING, today), "a bucket wasn't refused either");
        });
        helper.succeed();
    }

    // A dry cow turns the pail away with the bucket's own message, and the pail stays as it was.
    public static void aPailObeysTheMilkingRules(GameTestHelper helper) {
        Animal cow = BreedingGameTests.female(helper, EntityTypes.COW);
        List<Component> pailMessages = new ArrayList<>();
        ItemStack pail = new ItemStack(PailItems.IRON_PAIL);
        Player player = holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, pailMessages);
        List<Component> bucketMessages = new ArrayList<>();
        Player bucket = holding(helper, new ItemStack(Items.BUCKET), GameType.SURVIVAL, InteractionHand.MAIN_HAND, bucketMessages);

        GameTestCompat.interact(cow, player);
        GameTestCompat.interact(cow, bucket);

        assertHolds(helper, player.getMainHandItem(), pail, PailContents.EMPTY);
        helper.assertTrue(bucket.getMainHandItem().is(Items.BUCKET), "a dry cow gave " + bucket.getMainHandItem());
        helper.assertTrue(pailMessages.size() == 1 && bucketMessages.size() == 1
                        && translationKey(pailMessages.get(0)).equals("message.barnyard_romance.milk.none")
                        && translationKey(bucketMessages.get(0)).equals("message.barnyard_romance.milk.none"),
                "a dry cow told the pail " + pailMessages + " and the bucket " + bucketMessages);
        helper.succeed();
    }

    public static void aCancelledMilkingLeavesThePail(GameTestHelper helper) {
        assertListenerMilksNothing(helper, Milking::cancel);
    }

    public static void aListenerResultWithoutAFluidMilksNothing(GameTestHelper helper) {
        assertListenerMilksNothing(helper, milking -> milking.setResult(new ItemStack(Items.BREAD)));
    }

    private static void assertListenerMilksNothing(GameTestHelper helper, Consumer<Milking> listener) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        Lactation before = BreedingGameTests.traits(helper, cow).get(LACTATION);
        ItemStack pail = pail(PailItems.IRON_PAIL, MilkFluids.COW_ID, 1000);
        Player player = holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
        ApiGameTests.MILKINGS.add(cow.getUUID(), listener);
        try {
            GameTestCompat.interact(cow, player);
        } finally {
            ApiGameTests.MILKINGS.remove(cow.getUUID());
        }

        assertHolds(helper, player.getMainHandItem(), pail, new PailContents(MilkFluids.COW_ID, 1000));
        helper.assertTrue(BreedingGameTests.traits(helper, cow).get(LACTATION).equals(before), "the cow was counted as milked");
        helper.succeed();
    }

    // Creative mode keeps a bucket and hands over a new milk bucket; a pail fills where it is instead.
    public static void aCreativePlayerFillsThePail(GameTestHelper helper) {
        ItemStack pail = new ItemStack(PailItems.WOODEN_PAIL);
        Player player = holding(helper, pail, GameType.CREATIVE, InteractionHand.MAIN_HAND, new ArrayList<>());

        GameTestCompat.interact(ProductGameTests.mother(helper, EntityTypes.COW, 0.5), player);

        assertHolds(helper, player.getMainHandItem(), pail, new PailContents(MilkFluids.COW_ID, 1000));
        helper.assertTrue(!player.getInventory().contains(new ItemStack(Items.MILK_BUCKET)), "the creative player was handed a milk bucket");
        helper.succeed();
    }

    public static void anOffhandPailFills(GameTestHelper helper) {
        ItemStack pail = new ItemStack(PailItems.COPPER_PAIL);
        Player player = holding(helper, pail, GameType.SURVIVAL, InteractionHand.OFF_HAND, new ArrayList<>());

        GameTestCompat.interact(ProductGameTests.mother(helper, EntityTypes.GOAT, 0.5), player, InteractionHand.OFF_HAND);

        assertHolds(helper, player.getOffhandItem(), pail, new PailContents(MilkFluids.GOAT_ID, 1000));
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the main hand holds " + player.getMainHandItem());
        helper.succeed();
    }

    // An entry naming the iron pail comes before the bucket's, wherever it is in the list; another pail, which the list
    // doesn't name, takes the bucket's milk.
    public static void aConfigEntryNamingAPailWins(GameTestHelper helper) {
        UpkeepGameTests.withConfig("{\"species\": {\"minecraft:sheep\": {\"milk\": ["
                + "{\"bucket\": \"minecraft:bucket\", \"result\": \"barnyard_romance:sheep_milk_bucket\"}, "
                + "{\"bucket\": \"barnyard_romance:iron_pail\", \"result\": \"barnyard_romance:goat_milk_bucket\"}]}}}", () -> {
            ItemStack iron = new ItemStack(PailItems.IRON_PAIL);
            Player ironPlayer = holding(helper, iron, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
            GameTestCompat.interact(ProductGameTests.mother(helper, EntityTypes.SHEEP, 0.5), ironPlayer);
            assertHolds(helper, ironPlayer.getMainHandItem(), iron, new PailContents(MilkFluids.GOAT_ID, 1000));

            ItemStack wooden = new ItemStack(PailItems.WOODEN_PAIL);
            Player woodenPlayer = holding(helper, wooden, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
            GameTestCompat.interact(ProductGameTests.mother(helper, EntityTypes.SHEEP, 0.5), woodenPlayer);
            assertHolds(helper, woodenPlayer.getMainHandItem(), wooden, new PailContents(MilkFluids.SHEEP_ID, 1000));
        });
        helper.succeed();
    }

    // A use that no animal or block takes opens the window on the pail in that hand, here the offhand.
    public static void usingAPailOpensItsWindow(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.inLevel(helper);
        ItemStack pail = pail(PailItems.IRON_PAIL, MilkFluids.GOAT_ID, 2500);
        player.setItemInHand(InteractionHand.OFF_HAND, pail);

        pail.use(helper.getLevel(), player, InteractionHand.OFF_HAND);

        helper.assertTrue(player.containerMenu instanceof PailMenu, "using the pail opened " + player.containerMenu);
        PailMenu menu = (PailMenu) player.containerMenu;
        put(menu, player, new ItemStack(Items.BUCKET));
        assertHolds(helper, player.getOffhandItem(), pail, new PailContents(MilkFluids.GOAT_ID, 1500));
        assertSlot(helper, menu, MilkItems.GOAT_MILK_BUCKET);
        player.closeContainer();
        helper.succeed();
    }

    public static void anEmptyBucketTakesAThousand(GameTestHelper helper) {
        ItemStack pail = pail(PailItems.IRON_PAIL, MilkFluids.GOAT_ID, 2500);
        Player player = holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
        PailMenu menu = new PailMenu(1, player.getInventory(), InteractionHand.MAIN_HAND);

        put(menu, player, new ItemStack(Items.BUCKET));

        assertSlot(helper, menu, MilkItems.GOAT_MILK_BUCKET);
        helper.assertTrue(menu.getCarried().isEmpty(), "the cursor still holds " + menu.getCarried());
        assertHolds(helper, player.getMainHandItem(), pail, new PailContents(MilkFluids.GOAT_ID, 1500));
        helper.succeed();
    }

    public static void aMilkBucketPoursIn(GameTestHelper helper) {
        ItemStack pail = new ItemStack(PailItems.WOODEN_PAIL);
        Player player = holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
        PailMenu menu = new PailMenu(1, player.getInventory(), InteractionHand.MAIN_HAND);

        put(menu, player, new ItemStack(MilkItems.SHEEP_MILK_BUCKET));

        assertSlot(helper, menu, Items.BUCKET);
        assertHolds(helper, player.getMainHandItem(), pail, new PailContents(MilkFluids.SHEEP_ID, 1000));
        helper.succeed();
    }

    // Only buckets that trade with this pail go in: not another milk, not water, not anything else.
    public static void aDifferentMilkBucketIsRefused(GameTestHelper helper) {
        ItemStack pail = pail(PailItems.IRON_PAIL, MilkFluids.GOAT_ID, 1000);
        Player player = holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
        PailMenu menu = new PailMenu(1, player.getInventory(), InteractionHand.MAIN_HAND);

        for (Item refused : List.of(MilkItems.SHEEP_MILK_BUCKET, Items.WATER_BUCKET, Items.STICK)) {
            helper.assertTrue(!menu.getSlot(PailMenu.BUCKET_SLOT).mayPlace(new ItemStack(refused)), "the slot takes " + refused);
        }
        put(menu, player, new ItemStack(MilkItems.SHEEP_MILK_BUCKET));

        helper.assertTrue(menu.getCarried().is(MilkItems.SHEEP_MILK_BUCKET), "the sheep milk bucket became " + menu.getCarried());
        helper.assertTrue(!menu.getSlot(PailMenu.BUCKET_SLOT).hasItem(), "the slot holds " + menu.getSlot(PailMenu.BUCKET_SLOT).getItem());
        assertHolds(helper, player.getMainHandItem(), pail, new PailContents(MilkFluids.GOAT_ID, 1000));
        helper.succeed();
    }

    // A bucket moves exactly 1000 or nothing: 600 doesn't fill one, and 500 free doesn't take one.
    public static void aBucketNeedsAFullThousand(GameTestHelper helper) {
        ItemStack low = pail(PailItems.IRON_PAIL, MilkFluids.GOAT_ID, 600);
        Player lowPlayer = holding(helper, low, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
        PailMenu lowMenu = new PailMenu(1, lowPlayer.getInventory(), InteractionHand.MAIN_HAND);
        helper.assertTrue(!lowMenu.getSlot(PailMenu.BUCKET_SLOT).mayPlace(new ItemStack(Items.BUCKET)), "a pail at 600 takes an empty bucket");
        put(lowMenu, lowPlayer, new ItemStack(Items.BUCKET));
        assertHolds(helper, lowPlayer.getMainHandItem(), low, new PailContents(MilkFluids.GOAT_ID, 600));

        ItemStack high = pail(PailItems.WOODEN_PAIL, MilkFluids.GOAT_ID, 1500);
        Player highPlayer = holding(helper, high, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
        PailMenu highMenu = new PailMenu(1, highPlayer.getInventory(), InteractionHand.MAIN_HAND);
        helper.assertTrue(!highMenu.getSlot(PailMenu.BUCKET_SLOT).mayPlace(new ItemStack(MilkItems.GOAT_MILK_BUCKET)), "a pail with 500 free takes a bucket");
        put(highMenu, highPlayer, new ItemStack(MilkItems.GOAT_MILK_BUCKET));
        assertHolds(helper, highPlayer.getMainHandItem(), high, new PailContents(MilkFluids.GOAT_ID, 1500));
        helper.succeed();
    }

    public static void pourOutEmptiesThePail(GameTestHelper helper) {
        ItemStack pail = pail(PailItems.IRON_PAIL, MilkFluids.GOAT_ID, 2500);
        Player player = holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
        PailMenu menu = new PailMenu(1, player.getInventory(), InteractionHand.MAIN_HAND);

        boolean poured = menu.clickMenuButton(player, PailMenu.POUR_OUT);

        helper.assertTrue(poured, "pouring out wasn't taken");
        assertHolds(helper, player.getMainHandItem(), pail, PailContents.EMPTY);
        helper.succeed();
    }

    // Vanilla only hands a window's items back to a server player.
    public static void closingReturnsTheSlotItem(GameTestHelper helper) {
        ItemStack pail = pail(PailItems.IRON_PAIL, MilkFluids.GOAT_ID, 2500);
        ServerPlayer player = MockPlayers.inLevel(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, pail);
        PailMenu menu = new PailMenu(1, player.getInventory(), InteractionHand.MAIN_HAND);
        put(menu, player, new ItemStack(Items.BUCKET));

        menu.removed(player);

        helper.assertTrue(!menu.getSlot(PailMenu.BUCKET_SLOT).hasItem(), "the slot kept " + menu.getSlot(PailMenu.BUCKET_SLOT).getItem());
        helper.assertTrue(count(player, MilkItems.GOAT_MILK_BUCKET) == 1, "the player got " + count(player, MilkItems.GOAT_MILK_BUCKET) + " goat milk buckets back");
        assertHolds(helper, player.getMainHandItem(), pail, new PailContents(MilkFluids.GOAT_ID, 1500));
        helper.succeed();
    }

    // Shift-clicking a stack moves one bucket in, which becomes milk; the slot then holds it, so the rest stay put.
    public static void shiftClickingSixteenBucketsMovesOne(GameTestHelper helper) {
        ItemStack pail = pail(PailItems.IRON_PAIL, MilkFluids.GOAT_ID, 2500);
        Player player = holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
        player.getInventory().setItem(1, new ItemStack(Items.BUCKET, 16));
        PailMenu menu = new PailMenu(1, player.getInventory(), InteractionHand.MAIN_HAND);

        menu.clicked(menu.slots.size() - 9 + 1, 0, ContainerInput.QUICK_MOVE, player);

        assertSlot(helper, menu, MilkItems.GOAT_MILK_BUCKET);
        helper.assertTrue(player.getInventory().getItem(1).is(Items.BUCKET) && player.getInventory().getItem(1).getCount() == 15,
                "the hotbar holds " + player.getInventory().getItem(1));
        helper.assertTrue(count(player, Items.BUCKET) == 15, "the inventory holds " + count(player, Items.BUCKET) + " empty buckets");
        assertHolds(helper, player.getMainHandItem(), pail, new PailContents(MilkFluids.GOAT_ID, 1500));
        helper.succeed();
    }

    // However the pail leaves the hand the window is done with it: a copy put in its place, picked up with the cursor
    // (which leaves an emptied stack behind in the hand), or swapped to the offhand with F.
    public static void movingThePailClosesTheMenu(GameTestHelper helper) {
        ItemStack copied = pail(PailItems.IRON_PAIL, MilkFluids.GOAT_ID, 2500);
        Player copiedPlayer = holding(helper, copied, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
        PailMenu copiedMenu = new PailMenu(1, copiedPlayer.getInventory(), InteractionHand.MAIN_HAND);
        helper.assertTrue(copiedMenu.stillValid(copiedPlayer), "the window closes with the pail still in hand");
        copiedPlayer.setItemInHand(InteractionHand.MAIN_HAND, copied.copy());
        helper.assertTrue(!copiedMenu.stillValid(copiedPlayer), "the window stays open on a copy of the pail");

        ItemStack picked = pail(PailItems.IRON_PAIL, MilkFluids.GOAT_ID, 2500);
        Player pickedPlayer = holding(helper, picked, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
        PailMenu pickedMenu = new PailMenu(1, pickedPlayer.getInventory(), InteractionHand.MAIN_HAND);
        pickedMenu.clicked(pickedMenu.slots.size() - 9, 0, ContainerInput.PICKUP, pickedPlayer);
        helper.assertTrue(!pickedMenu.stillValid(pickedPlayer), "the window stays open with the pail on the cursor");
        helper.assertTrue(PailItem.contents(pickedMenu.getCarried()).equals(new PailContents(MilkFluids.GOAT_ID, 2500)),
                "the pail on the cursor holds " + PailItem.contents(pickedMenu.getCarried()));

        ItemStack swapped = pail(PailItems.IRON_PAIL, MilkFluids.GOAT_ID, 2500);
        Player swappedPlayer = holding(helper, swapped, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
        PailMenu swappedMenu = new PailMenu(1, swappedPlayer.getInventory(), InteractionHand.MAIN_HAND);
        swappedMenu.clicked(swappedMenu.slots.size() - 9, 40, ContainerInput.SWAP, swappedPlayer);
        helper.assertTrue(swappedPlayer.getOffhandItem() == swapped, "F didn't move the pail to the offhand: " + swappedPlayer.getOffhandItem());
        helper.assertTrue(!swappedMenu.stillValid(swappedPlayer), "the window stays open with the pail in the other hand");
        helper.succeed();
    }

    // A pail of a fluid that's gone (its mod removed) can still be poured out, but trades no buckets.
    public static void aDroppedFluidCanStillBePouredOut(GameTestHelper helper) {
        ItemStack pail = pail(PailItems.IRON_PAIL, Identifier.parse("gone:whey"), 1500);
        Player player = holding(helper, pail, GameType.SURVIVAL, InteractionHand.MAIN_HAND, new ArrayList<>());
        PailMenu menu = new PailMenu(1, player.getInventory(), InteractionHand.MAIN_HAND);

        for (Item refused : List.of(Items.BUCKET, MilkItems.GOAT_MILK_BUCKET, Items.MILK_BUCKET)) {
            helper.assertTrue(!menu.getSlot(PailMenu.BUCKET_SLOT).mayPlace(new ItemStack(refused)), "a pail of gone:whey takes " + refused);
        }
        boolean poured = menu.clickMenuButton(player, PailMenu.POUR_OUT);

        helper.assertTrue(poured, "pouring out wasn't taken");
        assertHolds(helper, player.getMainHandItem(), pail, PailContents.EMPTY);
        helper.succeed();
    }

    // The cursor puts this down on the window's slot, as a left click does.
    private static void put(PailMenu menu, Player player, ItemStack stack) {
        menu.setCarried(stack);
        menu.clicked(PailMenu.BUCKET_SLOT, 0, ContainerInput.PICKUP, player);
    }

    private static void assertSlot(GameTestHelper helper, PailMenu menu, Item expected) {
        ItemStack held = menu.getSlot(PailMenu.BUCKET_SLOT).getItem();
        helper.assertTrue(held.is(expected) && held.getCount() == 1, "the slot holds " + held + ", not one " + expected);
    }

    // How many of the item are anywhere in the player's inventory.
    private static int count(Player player, Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            count += stack.is(item) ? stack.getCount() : 0;
        }
        return count;
    }

    static ItemStack pail(Item item, Identifier fluid, int amount) {
        ItemStack pail = new ItemStack(item);
        PailItem.setContents(pail, new PailContents(fluid, amount));
        return pail;
    }

    // A mock player holding this very stack in the hand, keeping the overlay messages it's sent.
    static Player holding(GameTestHelper helper, ItemStack stack, GameType gameType, InteractionHand hand, List<Component> messages) {
        Player player = GameTestCompat.listeningPlayer(helper, gameType, messages);
        player.setItemInHand(hand, stack);
        return player;
    }

    // The pail is still the very stack the player held, filled where it is or left alone, and holds this.
    static void assertHolds(GameTestHelper helper, ItemStack held, ItemStack pail, PailContents expected) {
        helper.assertTrue(held == pail, "the pail in hand was replaced by " + held);
        helper.assertTrue(PailItem.contents(held).equals(expected), "the pail holds " + PailItem.contents(held) + ", not " + expected);
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
