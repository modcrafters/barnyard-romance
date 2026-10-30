package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.eggs.EggData;
import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.modcrafters.minecraft.barnyardromance.eggs.EggOutlook;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.modcrafters.minecraft.barnyardromance.eggs.ThrownEggs;
import net.modcrafters.minecraft.barnyardromance.eggs.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBookData;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.herd.HerdEntry;
import net.modcrafters.minecraft.barnyardromance.herd.HerdPageText;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.modcrafters.minecraft.barnyardromance.nest.LayInNestGoal;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxMenu;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.sex.Lineage;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
//? if >=1.21.2
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
//? if >=1.21.5 {
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
//?}

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.CareGameTests.clock;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;

// In-game checks for egg layers. Days are driven by setting the overworld clock and ticking the hen by hand,
// then setting the clock back, all within one tick.
public final class EggGameTests {
    // Chickens use the default sizes: females 0.7 to 0.9, so 0.76 is M and 0.9 is XL.
    static final double MEDIUM_HEN = 0.76;
    static final double LARGE_HEN = 0.9;
    static final BlockPos BOX = new BlockPos(2, 2, 2);
    // A box brings its countdowns up to date once a second; this is long enough for a nearly done egg to hatch.
    static final int COUNT_AND_HATCH = 30;
    // Ticks for a freshly spawned chicken to land: a mob in the air can't plan a path.
    static final int LANDED = 10;
    static final TagKey<Item> COMMON_EGGS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "eggs"));

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("hen_lays_her_daily_eggs_on_the_ground", 20, EggGameTests::henLaysHerDailyEggsOnTheGround),
            new BreedingGameTests.Case("large_happy_hen_plans_three", 20, EggGameTests::largeHappyHenPlansThree),
            new BreedingGameTests.Case("hungry_hen_plans_none", 20, EggGameTests::hungryHenPlansNone),
            new BreedingGameTests.Case("rooster_and_chick_lay_nothing", 20, EggGameTests::roosterAndChickLayNothing),
            new BreedingGameTests.Case("vanilla_egg_timer_is_off", 20, EggGameTests::vanillaEggTimerIsOff),
            new BreedingGameTests.Case("seeds_never_breed_chickens", 20, EggGameTests::seedsNeverBreedChickens),
            new BreedingGameTests.Case("old_chicken_pregnancy_is_cleared", 20, EggGameTests::oldChickenPregnancyIsCleared),
            new BreedingGameTests.Case("excluded_chickens_lay_like_vanilla", 20, EggGameTests::excludedChickensLayLikeVanilla),
            new BreedingGameTests.Case("fertilized_eggs_do_not_stack", 20, EggGameTests::fertilizedEggsDoNotStack),
            new BreedingGameTests.Case("fertilized_egg_is_an_egg", 20, EggGameTests::fertilizedEggIsAnEgg),
            new BreedingGameTests.Case("thrown_plain_egg_never_hatches", 20, EggGameTests::thrownPlainEggNeverHatches),
            new BreedingGameTests.Case("thrown_fertilized_egg_hatches_tainted", 20, EggGameTests::thrownFertilizedEggHatchesTainted),
            new BreedingGameTests.Case("thrown_fertilized_egg_uses_the_chance", 20, EggGameTests::thrownFertilizedEggUsesTheChance),
            new BreedingGameTests.Case("excluded_chickens_thrown_eggs_hatch", 20, EggGameTests::excludedChickensThrownEggsHatch),
            new BreedingGameTests.Case("box_takes_ten_eggs", 20, EggGameTests::boxTakesTenEggs),
            new BreedingGameTests.Case("plain_eggs_stack_in_one_slot", 20, EggGameTests::plainEggsStackInOneSlot),
            new BreedingGameTests.Case("plain_eggs_cannot_be_put_in", 20, EggGameTests::plainEggsCannotBePutIn),
            new BreedingGameTests.Case("hopper_pulls_only_plain_eggs", 20, EggGameTests::hopperPullsOnlyPlainEggs),
            new BreedingGameTests.Case("countdown_pauses_outside_the_box", 80, EggGameTests::countdownPausesOutsideTheBox),
            new BreedingGameTests.Case("box_drops_eggs_when_broken", 20, EggGameTests::boxDropsEggsWhenBroken),
            new BreedingGameTests.Case("box_recipe", 20, EggGameTests::boxRecipe),
            new BreedingGameTests.Case("egg_hatches_in_the_box", 40, EggGameTests::eggHatchesInTheBox),
            new BreedingGameTests.Case("hatched_chick_inherits", 20, EggGameTests::hatchedChickInherits),
            new BreedingGameTests.Case("hatched_chick_joins_mothers_book", 20, EggGameTests::hatchedChickJoinsMothersBook),
            new BreedingGameTests.Case("blocked_front_hatches_on_top", 20, EggGameTests::blockedFrontHatchesOnTop),
            new BreedingGameTests.Case("hatching_catches_up_after_unload", 20, EggGameTests::hatchingCatchesUpAfterUnload),
            new BreedingGameTests.Case("hen_lays_into_a_nearby_box", 400, EggGameTests::henLaysIntoANearbyBox),
            new BreedingGameTests.Case("box_egg_is_fertilized_with_a_fed_rooster", 400, EggGameTests::boxEggIsFertilizedWithAFedRooster),
            new BreedingGameTests.Case("heavy_hen_egg_hatches_sooner", 20, EggGameTests::heavyHenEggHatchesSooner),
            new BreedingGameTests.Case("closest_rooster_is_the_father", 20, EggGameTests::closestRoosterIsTheFather),
            new BreedingGameTests.Case("hungry_rooster_fertilizes_nothing", 20, EggGameTests::hungryRoosterFertilizesNothing),
            new BreedingGameTests.Case("ground_eggs_are_plain_with_a_rooster", 20, EggGameTests::groundEggsArePlainWithARooster),
            new BreedingGameTests.Case("far_box_is_ignored", 20, EggGameTests::farBoxIsIgnored),
            new BreedingGameTests.Case("second_hen_does_not_share_a_box", 40, EggGameTests::secondHenDoesNotShareABox),
            new BreedingGameTests.Case("unreachable_box_gives_up", 200, EggGameTests::unreachableBoxGivesUp),
            new BreedingGameTests.Case("sitting_hen_reserves_her_slot", 20, EggGameTests::sittingHenReservesHerSlot),
            new BreedingGameTests.Case("hen_in_box_stays_in_herd_book", 20, EggGameTests::henInBoxStaysInHerdBook),
            new BreedingGameTests.Case("breaking_box_releases_hen_and_eggs", 20, EggGameTests::breakingBoxReleasesHenAndEggs),
            new BreedingGameTests.Case("box_survives_save_with_hen", 20, EggGameTests::boxSurvivesSaveWithHen),
            new BreedingGameTests.Case("egg_due_while_sitting_is_laid_after", 20, EggGameTests::eggDueWhileSittingIsLaidAfter),
            new BreedingGameTests.Case("walking_hen_keeps_her_next_egg", 40, EggGameTests::walkingHenKeepsHerNextEgg),
            new BreedingGameTests.Case("leashed_hen_lays_on_the_ground", 100, EggGameTests::leashedHenLaysOnTheGround),
            new BreedingGameTests.Case("hen_comes_out_despite_a_copy", 20, EggGameTests::henComesOutDespiteACopy),
            new BreedingGameTests.Case("herd_book_shows_fertilized_eggs", 20, EggGameTests::herdBookShowsFertilizedEggs),
            new BreedingGameTests.Case("herd_book_says_why_eggs_are_plain", 20, EggGameTests::herdBookSaysWhyEggsArePlain),
            new BreedingGameTests.Case("herd_book_shows_no_eggs_for_an_unfed_hen", 20, EggGameTests::herdBookShowsNoEggsForAnUnfedHen),
            new BreedingGameTests.Case("herd_book_shows_no_eggs_for_roosters", 20, EggGameTests::herdBookShowsNoEggsForRoosters)
            //? if >=1.21.5 {
            , new BreedingGameTests.Case("warm_hen_lays_brown_eggs", 20, EggGameTests::warmHenLaysBrownEggs)
            //?}
    );

    private EggGameTests() {
    }

    public static void henLaysHerDailyEggsOnTheGround(GameTestHelper helper) {
        Animal hen = hen(helper, MEDIUM_HEN, 0.0);

        Laying planned = tickAt(helper, hen, afterMidnight(10L));
        helper.assertTrue(planned.day() == 10L && planned.times().size() == 1, "an M hen planned " + planned);
        Laying after = tickAt(helper, hen, at(10L, planned.times().getFirst()));

        helper.assertTrue(after.next() == 1, "the egg wasn't counted: " + after);
        helper.assertTrue(eggsNear(helper, hen, Items.EGG) == 1, "an M hen laid " + eggsNear(helper, hen, Items.EGG) + " eggs");
        helper.succeed();
    }

    public static void largeHappyHenPlansThree(GameTestHelper helper) {
        Animal hen = hen(helper, LARGE_HEN, 0.5);

        Laying planned = tickAt(helper, hen, afterMidnight(10L));

        helper.assertTrue(planned.times().size() == 3, "an XL happy hen planned " + planned);
        helper.succeed();
    }

    public static void hungryHenPlansNone(GameTestHelper helper) {
        Animal hen = hen(helper, LARGE_HEN, 0.5);
        Services.DATA.set(hen, traits(helper, hen).withHunger(new Hunger(10L, -1L, 9L)));

        Laying planned = tickAt(helper, hen, afterMidnight(10L));

        helper.assertTrue(planned.times().isEmpty(), "a hen that went hungry yesterday planned " + planned);
        helper.succeed();
    }

    public static void roosterAndChickLayNothing(GameTestHelper helper) {
        Animal rooster = chicken(helper, Sex.MALE, 1.2, 0.5, 3);
        Animal chick = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 5);
        chick.setBaby(true);

        Laying roosterPlan = tickAt(helper, rooster, afterMidnight(10L));
        Laying chickPlan = tickAt(helper, chick, afterMidnight(10L));

        helper.assertTrue(roosterPlan.times().isEmpty(), "a rooster planned " + roosterPlan);
        helper.assertTrue(chickPlan.times().isEmpty(), "a chick planned " + chickPlan);
        helper.succeed();
    }

    public static void vanillaEggTimerIsOff(GameTestHelper helper) {
        Chicken hen = (Chicken) hen(helper, MEDIUM_HEN, 0.0);
        hen.eggTime = 1;

        hen.aiStep();

        helper.assertTrue(eggsNear(helper, hen, Items.EGG) == 0, "vanilla's egg timer laid an egg");
        helper.succeed();
    }

    public static void seedsNeverBreedChickens(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 1.0, 3);
        Animal rooster = chicken(helper, Sex.MALE, 1.2, 1.0, 5);

        FamiliarityActions.interact(hen, holding(helper, Items.WHEAT_SEEDS), InteractionHand.MAIN_HAND);
        FamiliarityActions.interact(rooster, holding(helper, Items.WHEAT_SEEDS), InteractionHand.MAIN_HAND);
        hen.setInLove(null);

        helper.assertTrue(!hen.isInLove() && !rooster.isInLove(), "seeds put chickens in love");
        helper.assertTrue(traits(helper, hen).lastFedTick() >= 0L, "seeds didn't feed the hen");
        helper.succeed();
    }

    public static void oldChickenPregnancyIsCleared(GameTestHelper helper) {
        Animal hen = hen(helper, MEDIUM_HEN, 0.5);
        Services.DATA.setPregnancy(hen, new Pregnancy(0L, 1L, "minecraft:chicken", 1.2, 1, 0.5));

        EggLaying.serverTick(hen, helper.getLevel());

        helper.assertTrue(Services.DATA.getPregnancy(hen).isEmpty(), "a hen kept her pregnancy");
        helper.succeed();
    }

    // Vanilla only starts love for a real server player, which a mock player isn't, so love is set directly:
    // the mod's love gate is what's being checked.
    public static void excludedChickensLayLikeVanilla(GameTestHelper helper) {
        Chicken hen = (Chicken) hen(helper, MEDIUM_HEN, 0.0);
        Chicken other = (Chicken) chicken(helper, Sex.MALE, 1.2, 1.0, 5);
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"minecraft:chicken\"]}"));
            hen.eggTime = 1;
            hen.aiStep();
            hen.setInLove(null);
            other.setInLove(null);
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(eggsNear(helper, hen, Items.EGG) == 1, "an excluded chicken didn't lay by vanilla's timer");
        helper.assertTrue(hen.isInLove() && other.isInLove(), "excluded chickens couldn't fall in love");
        helper.succeed();
    }

    //? if >=1.21.5 {
    public static void warmHenLaysBrownEggs(GameTestHelper helper) {
        Chicken hen = (Chicken) hen(helper, MEDIUM_HEN, 0.0);
        hen.setVariant(helper.getLevel().registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT).getOrThrow(ChickenVariants.WARM));

        Laying planned = tickAt(helper, hen, afterMidnight(10L));
        tickAt(helper, hen, at(10L, planned.times().getFirst()));

        helper.assertTrue(eggsNear(helper, hen, Items.BROWN_EGG) == 1, "a warm hen didn't lay a brown egg");
        helper.succeed();
    }
    //?}

    public static void fertilizedEggsDoNotStack(GameTestHelper helper) {
        ItemStack first = FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L));
        ItemStack second = FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L));
        SimpleContainer container = new SimpleContainer(2);

        container.addItem(first);
        container.addItem(second);

        helper.assertTrue(first.getMaxStackSize() == 1, "a fertilized egg stacks to " + first.getMaxStackSize());
        helper.assertTrue(container.getItem(0).getCount() == 1 && container.getItem(1).getCount() == 1, "two fertilized eggs shared a slot");
        helper.succeed();
    }

    public static void fertilizedEggIsAnEgg(GameTestHelper helper) {
        ItemStack egg = FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L));
        CraftingInput input = CraftingInput.of(3, 3, List.of(
                new ItemStack(Items.MILK_BUCKET), new ItemStack(Items.MILK_BUCKET), new ItemStack(Items.MILK_BUCKET),
                new ItemStack(Items.SUGAR), egg, new ItemStack(Items.SUGAR),
                new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT)
        ));

        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());

        helper.assertTrue(egg.is(COMMON_EGGS), "a fertilized egg isn't in c:eggs");
        helper.assertTrue(recipe.isPresent(), "cake doesn't take a fertilized egg");
        helper.succeed();
    }

    public static void thrownPlainEggNeverHatches(GameTestHelper helper) {
        for (int i = 0; i < 64; i++) {
            throwAndLand(helper, new ItemStack(Items.EGG));
        }

        helper.assertTrue(chickensIn(helper) == 0, "a thrown plain egg hatched " + chickensIn(helper) + " chickens");
        helper.succeed();
    }

    public static void thrownFertilizedEggHatchesTainted(GameTestHelper helper) {
        EggData data = eggData(LARGE_HEN, 0.7, 1_000L);

        Animal chick = ThrownEggs.hatchTainted(helper.getLevel(), helper.absoluteVec(new Vec3(4.5, 2.0, 4.5)), data, helper.getLevel().getRandom())
                .orElseThrow(() -> GameTestCompat.failure(helper, "no chick hatched"));

        AnimalTraits traits = traits(helper, chick);
        SizeClass size = SizeClass.of(traits.sex(), traits.scale(), BarnyardConfig.current().sizeFor("minecraft:chicken"));
        helper.assertTrue(chick.isBaby(), "a tainted chick was born grown");
        helper.assertTrue(size == SizeClass.S || size == SizeClass.M, "a tainted chick is " + size);
        helper.assertTrue(traits.weight() == 0.25, "a tainted chick weighs " + traits.weight());
        helper.assertTrue(traits.lineage().equals(Lineage.UNKNOWN), "a tainted chick knows its parents: " + traits.lineage());
        helper.assertTrue(traits.bornTick() >= 0L, "a tainted chick has no birth time");
        helper.succeed();
    }

    public static void thrownFertilizedEggUsesTheChance(GameTestHelper helper) {
        BarnyardConfig config = BarnyardConfig.current();
        int always;
        int never;
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"species\": {\"minecraft:chicken\": {\"eggs\": {\"thrownHatchChance\": 1.0}}}}"));
            throwAndLand(helper, FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
            always = chickensIn(helper);
            BarnyardConfig.set(BarnyardConfig.parse("{\"species\": {\"minecraft:chicken\": {\"eggs\": {\"thrownHatchChance\": 0.0}}}}"));
            throwAndLand(helper, FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
            never = chickensIn(helper) - always;
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(always == 1, "a sure fertilized egg hatched " + always + " chicks");
        helper.assertTrue(never == 0, "a fertilized egg that can't hatch made " + never + " chicks");
        helper.succeed();
    }

    public static void excludedChickensThrownEggsHatch(GameTestHelper helper) {
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"minecraft:chicken\"]}"));
            for (int i = 0; i < 64; i++) {
                throwAndLand(helper, new ItemStack(Items.EGG));
            }
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(chickensIn(helper) > 0, "64 eggs thrown with chickens excluded hatched nothing");
        helper.succeed();
    }

    public static void boxTakesTenEggs(GameTestHelper helper) {
        NestingBoxBlockEntity fertile = box(helper, BOX);
        NestingBoxBlockEntity plain = box(helper, BOX.east(2));

        for (int i = 0; i < NestingBoxBlockEntity.SLOTS; i++) {
            helper.assertTrue(fertile.placeEgg(FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L))), "fertilized egg " + (i + 1) + " didn't fit");
        }
        boolean eleventh = fertile.placeEgg(FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
        for (int i = 0; i < NestingBoxBlockEntity.SLOTS * 16; i++) {
            helper.assertTrue(plain.placeEgg(new ItemStack(Items.EGG)), "plain egg " + (i + 1) + " didn't fit");
        }
        boolean extra = plain.placeEgg(new ItemStack(Items.EGG));

        helper.assertTrue(!eleventh, "an eleventh fertilized egg fit");
        helper.assertTrue(!extra, "a 161st plain egg fit");
        helper.succeed();
    }

    public static void plainEggsStackInOneSlot(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);

        for (int i = 0; i < 3; i++) {
            box.placeEgg(new ItemStack(Items.EGG));
        }

        helper.assertTrue(box.getItem(0).getCount() == 3 && box.getItem(1).isEmpty(), "three plain eggs: " + box.getItem(0) + ", " + box.getItem(1));
        helper.succeed();
    }

    public static void plainEggsCannotBePutIn(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        box.placeEgg(new ItemStack(Items.EGG));
        Player player = holding(helper, Items.EGG);
        NestingBoxMenu menu = new NestingBoxMenu(1, player.getInventory(), box);
        ItemStack fertile = FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L));

        boolean plainAllowed = menu.getSlot(0).mayPlace(new ItemStack(Items.EGG));
        boolean fertileAllowed = menu.getSlot(1).mayPlace(fertile);
        boolean ontoAnEgg = menu.getSlot(0).mayPlace(fertile);
        //? if >=1.21.5 {
        int held = menu.slots.size() - 9 + player.getInventory().getSelectedSlot();
        //?} else
        //int held = menu.slots.size() - 9 + player.getInventory().selected;
        menu.quickMoveStack(player, held);

        helper.assertTrue(!plainAllowed, "a plain egg can be put in a nest by hand");
        helper.assertTrue(fertileAllowed, "a fertilized egg can't be put in an empty nest");
        helper.assertTrue(!ontoAnEgg, "a fertilized egg can be put on top of another egg");
        helper.assertTrue(box.getItem(0).getCount() == 1 && box.getItem(1).isEmpty(), "shift-clicking moved a plain egg into the box");
        helper.succeed();
    }

    public static void hopperPullsOnlyPlainEggs(GameTestHelper helper) {
        helper.setBlock(BOX.below(), Blocks.HOPPER.defaultBlockState());
        NestingBoxBlockEntity box = box(helper, BOX);
        box.placeEgg(new ItemStack(Items.EGG, 2));
        box.placeEgg(FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
        HopperBlockEntity hopper = (HopperBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(BOX.below()));

        for (int i = 0; i < 4; i++) {
            HopperBlockEntity.suckInItems(helper.getLevel(), hopper);
        }

        helper.assertTrue(box.getItem(0).isEmpty(), "the hopper left plain eggs: " + box.getItem(0));
        helper.assertTrue(box.getItem(1).is(BarnyardItems.FERTILIZED_EGG), "the hopper took the fertilized egg");
        helper.succeed();
    }

    // The box counts its eggs down; an egg taken out any way at all, shift-click included, stops counting.
    public static void countdownPausesOutsideTheBox(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        Player player = holding(helper, Items.STICK);
        NestingBoxMenu menu = new NestingBoxMenu(1, player.getInventory(), box);
        box.setItem(0, FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));

        helper.runAfterDelay(25, () -> {
            long inBox = FertilizedEggItem.data(box.getItem(0)).orElseThrow().remainingTicks();
            helper.assertTrue(inBox < 1_000L, "an egg in the box didn't count down: " + inBox + " left");
            menu.quickMoveStack(player, 0);
            ItemStack carried = carriedEgg(helper, player);
            long out = FertilizedEggItem.data(carried).orElseThrow().remainingTicks();
            helper.runAfterDelay(25, () -> {
                long later = FertilizedEggItem.data(carriedEgg(helper, player)).orElseThrow().remainingTicks();
                helper.assertTrue(later == out, "a shift-clicked egg kept counting: " + out + " then " + later);
                helper.succeed();
            });
        });
    }

    static ItemStack carriedEgg(GameTestHelper helper, Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(BarnyardItems.FERTILIZED_EGG)) {
                return player.getInventory().getItem(slot);
            }
        }
        throw GameTestCompat.failure(helper, "the egg didn't reach the player's inventory");
    }

    public static void boxDropsEggsWhenBroken(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        box.placeEgg(new ItemStack(Items.EGG, 3));
        box.placeEgg(FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));

        helper.destroyBlock(BOX);

        List<ItemEntity> dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(BOX)).inflate(2.0));
        int plain = dropped.stream().filter(item -> item.getItem().is(Items.EGG)).mapToInt(item -> item.getItem().getCount()).sum();
        List<EggData> fertile = dropped.stream().map(item -> FertilizedEggItem.data(item.getItem())).flatMap(Optional::stream).toList();
        helper.assertTrue(plain == 3, "breaking the box dropped " + plain + " plain eggs");
        helper.assertTrue(fertile.size() == 1, "breaking the box dropped fertilized eggs " + fertile);
        helper.succeed();
    }

    public static void boxRecipe(GameTestHelper helper) {
        ItemStack plank = new ItemStack(Items.OAK_PLANKS);
        CraftingInput input = CraftingInput.of(3, 3, List.of(
                plank.copy(), ItemStack.EMPTY, plank.copy(),
                plank.copy(), new ItemStack(Items.WHEAT), plank.copy(),
                plank.copy(), plank.copy(), plank.copy()
        ));

        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());

        helper.assertTrue(recipe.isPresent(), "planks round wheat craft nothing");
        //? if >=1.21.2 {
        ItemStack result = recipe.get().value().assemble(input);
        //?} else
        //ItemStack result = recipe.get().value().assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(result.is(BarnyardBlocks.NESTING_BOX_ITEM), "planks round wheat craft " + result);
        helper.succeed();
    }

    public static void eggHatchesInTheBox(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        box.setItem(0, FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 5L)));

        helper.runAfterDelay(COUNT_AND_HATCH, () -> {
            helper.assertTrue(box.getItem(0).isEmpty(), "the egg is still in the box: " + box.getItem(0));
            // The test box stands a block above the floor, so the chick may have dropped down by now.
            List<Chicken> chicks = helper.getLevel().getEntitiesOfClass(Chicken.class, new AABB(helper.absolutePos(BOX.north())).expandTowards(0.0, -2.0, 0.0));
            helper.assertTrue(chicks.size() == 1 && chicks.getFirst().isBaby(), "in front of the hole: " + chicks);
            helper.succeed();
        });
    }

    public static void hatchedChickInherits(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        EggData data = new EggData("minecraft:chicken", UUID.randomUUID(), UUID.randomUUID(), LARGE_HEN, Growth.wild(), 1.0, 0.5,
                1.3, Growth.wild(), 1.0, "", 1_000L, 0L);
        box.setItem(0, FertilizedEggItem.create(data));

        NestingBoxBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(BOX), helper.getBlockState(BOX), box);

        Animal chick = chickIn(helper, BOX.north());
        AnimalTraits traits = traits(helper, chick);
        SizeClass size = SizeClass.of(traits.sex(), traits.scale(), BarnyardConfig.current().sizeFor("minecraft:chicken"));
        helper.assertTrue(traits.lineage().equals(new Lineage(data.mother(), data.father())), "the chick's parents are " + traits.lineage());
        helper.assertTrue(traits.bornTick() == helper.getLevel().getGameTime(), "the chick was born at " + traits.bornTick());
        helper.assertTrue(traits.familiarity() == Familiarity.inherited(0.5), "the chick starts at familiarity " + traits.familiarity());
        helper.assertTrue(size != SizeClass.S, "two XL parents hatched an S chick");
        helper.succeed();
    }

    public static void hatchedChickJoinsMothersBook(GameTestHelper helper) {
        Animal mother = hen(helper, LARGE_HEN, 0.5);
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        HerdBooks.register(book, mother);
        UUID bookId = book.get(BarnyardComponents.HERD_BOOK);
        UUID motherId = mother.getUUID();
        mother.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        NestingBoxBlockEntity box = box(helper, BOX);
        EggData data = new EggData("minecraft:chicken", motherId, null, LARGE_HEN, Growth.wild(), 0.5, 0.5,
                1.2, Growth.wild(), 0.5, "", 1_000L, 0L);
        box.setItem(0, FertilizedEggItem.create(data));

        NestingBoxBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(BOX), helper.getBlockState(BOX), box);

        Animal chick = chickIn(helper, BOX.north());
        HerdBookData herd = HerdBookData.get(helper.getLevel().getServer());
        helper.assertTrue(herd.ledger().isMember(bookId, chick.getUUID()), "the chick didn't join its mother's book");
        helper.succeed();
    }

    public static void blockedFrontHatchesOnTop(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        helper.setBlock(BOX.north(), Blocks.STONE.defaultBlockState());
        box.setItem(0, FertilizedEggItem.create(new EggData("minecraft:chicken", UUID.randomUUID(), null, LARGE_HEN, Growth.wild(), 0.5, 0.5,
                1.2, Growth.wild(), 0.5, "", 1_000L, 0L)));

        NestingBoxBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(BOX), helper.getBlockState(BOX), box);

        chickIn(helper, BOX.above());
        helper.succeed();
    }

    // A box saved long ago counts the time it was unloaded on its first tick back, and an egg that ran out hatches.
    public static void hatchingCatchesUpAfterUnload(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        box.setItem(0, FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
        NestingBoxBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(BOX), helper.getBlockState(BOX), box);
        CompoundTag saved = box.saveWithFullMetadata(helper.getLevel().registryAccess());
        saved.putLong("lastCounted", helper.getLevel().getGameTime() - 50_000L);
        helper.getLevel().removeBlockEntity(helper.absolutePos(BOX));

        NestingBoxBlockEntity loaded = (NestingBoxBlockEntity) BlockEntity.loadStatic(helper.absolutePos(BOX), helper.getBlockState(BOX), saved, helper.getLevel().registryAccess());
        helper.getLevel().setBlockEntity(loaded);
        NestingBoxBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(BOX), helper.getBlockState(BOX), loaded);

        helper.assertTrue(loaded.getItem(0).isEmpty(), "an egg that ran out while unloaded didn't hatch: " + FertilizedEggItem.data(loaded.getItem(0))
                + ", saved lastCounted " + saved.get("lastCounted") + ", now " + helper.getLevel().getGameTime());
        chickIn(helper, BOX.north());
        helper.succeed();
    }

    public static void henLaysIntoANearbyBox(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 2);
        NestingBoxBlockEntity[] placed = new NestingBoxBlockEntity[1];

        helper.runAfterDelay(LANDED, () -> {
            placed[0] = boxBeside(helper, hen, 5);
            inPen(() -> EggLaying.layDue(hen, helper.getLevel()));
        });

        once(helper, () -> placed[0] != null && placed[0].occupied(), () -> {
            NestingBoxBlockEntity box = placed[0];
            box.finishSitting();
            helper.assertTrue(box.getItem(0).is(Items.EGG), "the box holds " + box.getItem(0));
            helper.assertTrue(eggsNear(helper, hen, Items.EGG) == 0, "she laid on the ground too");
            helper.assertTrue(!box.occupied(), "she's still in the box");
            helper.succeed();
        });
    }

    public static void boxEggIsFertilizedWithAFedRooster(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 2);
        Animal rooster = chicken(helper, Sex.MALE, 1.2, 0.5, 5);
        NestingBoxBlockEntity[] placed = new NestingBoxBlockEntity[1];

        helper.runAfterDelay(LANDED, () -> {
            placed[0] = boxBeside(helper, hen, 5);
            inPen(() -> EggLaying.layDue(hen, helper.getLevel()));
        });

        once(helper, () -> placed[0] != null && placed[0].occupied(), () -> {
            NestingBoxBlockEntity box = placed[0];
            box.finishSitting();
            EggData egg = FertilizedEggItem.data(box.getItem(0)).orElseThrow(() -> GameTestCompat.failure(helper, "the box egg is " + box.getItem(0)));
            helper.assertTrue(rooster.getUUID().equals(egg.father()), "the father is " + egg.father());
            helper.assertTrue(hen.getUUID().equals(egg.mother()), "the mother is " + egg.mother());
            helper.succeed();
        });
    }

    public static void heavyHenEggHatchesSooner(GameTestHelper helper) {
        Animal heavy = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 2);
        Animal light = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 3);
        chicken(helper, Sex.MALE, 1.2, 0.5, 5);
        Services.DATA.set(heavy, traits(helper, heavy).withWeight(1.0));
        Services.DATA.set(light, traits(helper, light).withWeight(0.0));
        BarnyardConfig config = BarnyardConfig.current();
        long heavyTotal = 0L;
        long lightTotal = 0L;
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"species\": {\"minecraft:chicken\": {\"eggs\": {\"hatchTicksMin\": 100, \"hatchTicksMax\": 200}}}}"));
            for (int i = 0; i < 20; i++) {
                heavyTotal += fertile(helper, heavy).totalTicks();
                lightTotal += fertile(helper, light).totalTicks();
            }
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(heavyTotal < lightTotal, "heavy hens' eggs take " + heavyTotal + " ticks, light hens' " + lightTotal);
        helper.succeed();
    }

    public static void closestRoosterIsTheFather(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 2);
        Animal near = chicken(helper, Sex.MALE, 1.2, 0.5, 4);
        chicken(helper, Sex.MALE, 1.2, 0.5, 8);

        EggData egg = fertile(helper, hen);

        helper.assertTrue(near.getUUID().equals(egg.father()), "the father isn't the closest rooster");
        helper.succeed();
    }

    public static void hungryRoosterFertilizesNothing(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 2);
        Animal rooster = chicken(helper, Sex.MALE, 1.2, 0.5, 4);
        long today = 10L;
        Services.DATA.set(rooster, traits(helper, rooster).withHunger(new Hunger(today, -1L, today - 1L)));

        Optional<ItemStack>[] egg = new Optional[1];
        long before = helper.getLevel().getOverworldClockTime();
        try {
            GameTestCompat.setOverworldClock(helper, afterMidnight(today) + 1_000L);
            inPen(() -> egg[0] = EggLaying.fertilizedEgg(hen, helper.getLevel()));
        } finally {
            GameTestCompat.setOverworldClock(helper, before);
        }

        helper.assertTrue(egg[0].isEmpty(), "a rooster that went hungry yesterday fertilized an egg: father "
                + egg[0].flatMap(FertilizedEggItem::data).map(EggData::father).orElse(null) + ", the rooster " + rooster.getUUID()
                + ", hunger " + traits(helper, rooster).hunger() + ", today " + today);
        helper.succeed();
    }

    public static void groundEggsArePlainWithARooster(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 2);
        chicken(helper, Sex.MALE, 1.2, 0.5, 3);

        inPen(() -> EggLaying.layDue(hen, helper.getLevel()));

        helper.assertTrue(eggsNear(helper, hen, Items.EGG) == 1, "no plain egg on the ground");
        helper.assertTrue(eggsNear(helper, hen, BarnyardItems.FERTILIZED_EGG) == 0, "a fertilized egg on the ground");
        helper.succeed();
    }

    public static void farBoxIsIgnored(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 2);
        boxBeside(helper, hen, 5);
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"species\": {\"minecraft:chicken\": {\"eggs\": {\"boxRange\": 3}}}}"));
            EggLaying.layDue(hen, helper.getLevel());
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(eggsNear(helper, hen, Items.EGG) == 1, "a hen went for a box out of range");
        helper.succeed();
    }

    public static void secondHenDoesNotShareABox(GameTestHelper helper) {
        Animal first = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 2);
        Animal second = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 3);
        Animal third = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 4);

        helper.runAfterDelay(LANDED, () -> {
            NestingBoxBlockEntity taken = boxBeside(helper, second, 4);
            NestingBoxBlockEntity free = boxAt(helper, penFloor(helper, second, 7, 7));
            helper.assertTrue(taken.enter(first, new ItemStack(Items.EGG), helper.getLevel().getGameTime()), "the first hen couldn't get in");
            inPen(() -> EggLaying.layDue(second, helper.getLevel()));
            BlockPos chosen = LayInNestGoal.target(second).orElse(null);
            helper.getLevel().removeBlock(free.getBlockPos(), false);
            inPen(() -> EggLaying.layDue(third, helper.getLevel()));

            helper.assertTrue(free.getBlockPos().equals(chosen), "the second hen headed for " + chosen + ", not the free box at " + free.getBlockPos());
            helper.assertTrue(eggsNear(helper, third, Items.EGG) == 1 && LayInNestGoal.target(third).isEmpty(), "a hen with only a taken box didn't lay on the ground");
            helper.succeed();
        });
    }

    public static void unreachableBoxGivesUp(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 2);

        helper.runAfterDelay(LANDED, () -> {
            BlockPos at = boxBeside(helper, hen, 5).getBlockPos();
            for (Direction side : Direction.Plane.HORIZONTAL) {
                helper.getLevel().setBlockAndUpdate(at.relative(side), Blocks.STONE.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(at.relative(side).above(), Blocks.STONE.defaultBlockState());
            }
            helper.getLevel().setBlockAndUpdate(at.above(), Blocks.STONE.defaultBlockState());
            inPen(() -> EggLaying.layDue(hen, helper.getLevel()));
        });

        once(helper, () -> eggsNear(helper, hen, Items.EGG) == 1, () -> {
            helper.assertTrue(LayInNestGoal.target(hen).isEmpty(), "she's still heading for the walled-in box");
            helper.succeed();
        });
    }

    public static void sittingHenReservesHerSlot(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        for (int i = 0; i < NestingBoxBlockEntity.SLOTS - 1; i++) {
            box.placeEgg(FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
        }
        Animal hen = hen(helper, MEDIUM_HEN, 0.5);
        box.enter(hen, new ItemStack(Items.EGG), helper.getLevel().getGameTime());
        NestingBoxMenu menu = new NestingBoxMenu(1, holding(helper, Items.STICK).getInventory(), box);

        boolean playerFits = menu.getSlot(NestingBoxBlockEntity.SLOTS - 1).mayPlace(FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
        boolean boxFits = box.placeEgg(FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));

        helper.assertTrue(!playerFits && !boxFits, "the sitting hen's nest was taken");
        helper.succeed();
    }

    public static void henInBoxStaysInHerdBook(GameTestHelper helper) {
        Animal hen = hen(helper, MEDIUM_HEN, 0.6);
        hen.setCustomName(net.minecraft.network.chat.Component.literal("Henrietta"));
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), hen);
        UUID id = hen.getUUID();
        NestingBoxBlockEntity box = box(helper, BOX);
        HerdBookData herd = HerdBookData.get(helper.getLevel().getServer());

        box.enter(hen, new ItemStack(Items.EGG), helper.getLevel().getGameTime());
        boolean aliveInside = herd.record(id).alive() && herd.ledger().node(id).alive();
        box.finishSitting();

        Entity back = helper.getLevel().getEntity(id);
        helper.assertTrue(aliveInside, "the herd book thinks the hen in the box died");
        helper.assertTrue(back instanceof Animal, "the hen didn't come back out");
        helper.assertTrue(back.getCustomName() != null && back.getCustomName().getString().equals("Henrietta"), "the hen lost her name");
        helper.assertTrue(traits(helper, (Animal) back).familiarity() == 0.6, "the hen lost her familiarity");
        helper.succeed();
    }

    public static void breakingBoxReleasesHenAndEggs(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        box.placeEgg(FertilizedEggItem.create(eggData(LARGE_HEN, 1.2, 1_000L)));
        Animal hen = hen(helper, MEDIUM_HEN, 0.5);
        UUID id = hen.getUUID();
        box.enter(hen, new ItemStack(Items.EGG), helper.getLevel().getGameTime());

        helper.destroyBlock(BOX);

        helper.assertTrue(helper.getLevel().getEntity(id) instanceof Animal, "breaking the box didn't let the hen out");
        List<ItemEntity> dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(BOX)).inflate(2.0));
        helper.assertTrue(dropped.stream().anyMatch(item -> item.getItem().is(BarnyardItems.FERTILIZED_EGG)), "breaking the box lost its egg");
        helper.assertTrue(dropped.stream().anyMatch(item -> item.getItem().is(Items.EGG)), "breaking the box lost the sitting hen's egg");
        helper.succeed();
    }

    public static void boxSurvivesSaveWithHen(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        Animal hen = hen(helper, MEDIUM_HEN, 0.5);
        UUID id = hen.getUUID();
        box.enter(hen, new ItemStack(Items.EGG), helper.getLevel().getGameTime());
        CompoundTag saved = box.saveWithFullMetadata(helper.getLevel().registryAccess());
        helper.getLevel().removeBlockEntity(helper.absolutePos(BOX));

        BlockEntity loaded = BlockEntity.loadStatic(helper.absolutePos(BOX), helper.getBlockState(BOX), saved, helper.getLevel().registryAccess());
        helper.getLevel().setBlockEntity(loaded);
        ((NestingBoxBlockEntity) loaded).finishSitting();

        helper.assertTrue(helper.getLevel().getEntity(id) instanceof Animal, "the hen was lost when the box was saved");
        helper.assertTrue(((NestingBoxBlockEntity) loaded).getItem(0).is(Items.EGG), "the saved box lost her egg");
        helper.succeed();
    }

    // Box and rooster ranges small enough that other tests' pens, laid out alongside, are out of reach. Swapped in
    // and back within the tick, like every config change in these tests.
    static void inPen(Runnable action) {
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"species\": {\"minecraft:chicken\": {\"eggs\": {\"boxRange\": 8, \"roosterRange\": 4}}}}"));
            action.run();
        } finally {
            BarnyardConfig.set(config);
        }
    }

    // An egg that came due while the hen sat in a box is laid when she comes out, not skipped.
    public static void eggDueWhileSittingIsLaidAfter(GameTestHelper helper) {
        Animal hen = hen(helper, MEDIUM_HEN, 0.0);
        UUID id = hen.getUUID();
        NestingBoxBlockEntity box = box(helper, BOX);
        long day = 10L;
        long clock = at(day, 10_000);
        long before = helper.getLevel().getOverworldClockTime();
        BarnyardConfig config = BarnyardConfig.current();
        Animal back;
        try {
            GameTestCompat.setOverworldClock(helper, clock);
            Services.DATA.setLaying(hen, new Laying(day, List.of(9_500, 15_000), 0));
            box.enter(hen, new ItemStack(Items.EGG), helper.getLevel().getGameTime());
            box.finishSitting();
            back = (Animal) helper.getLevel().getEntity(id);
            BarnyardConfig.set(BarnyardConfig.parse("{\"species\": {\"minecraft:chicken\": {\"eggs\": {\"boxRange\": 0, \"roosterRange\": 0}}}}"));
            EggLaying.serverTick(back, helper.getLevel());
        } finally {
            BarnyardConfig.set(config);
            GameTestCompat.setOverworldClock(helper, before);
        }

        helper.assertTrue(eggsNear(helper, back, Items.EGG) == 1, "the egg due while she sat was skipped");
        helper.succeed();
    }

    // A second egg coming due on the way to a box waits; it doesn't send her off on a new trip or vanish.
    public static void walkingHenKeepsHerNextEgg(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.0, 2);

        helper.runAfterDelay(LANDED, () -> {
            BlockPos box = boxBeside(helper, hen, 5).getBlockPos();
            inPen(() -> EggLaying.layDue(hen, helper.getLevel()));
            long day = 10L;
            long before = helper.getLevel().getOverworldClockTime();
            Laying after;
            try {
                GameTestCompat.setOverworldClock(helper, at(day, 10_000));
                Services.DATA.setLaying(hen, new Laying(day, List.of(10_000, 15_000), 0));
                inPen(() -> EggLaying.serverTick(hen, helper.getLevel()));
                after = Services.DATA.getLaying(hen).orElseThrow();
            } finally {
                GameTestCompat.setOverworldClock(helper, before);
            }

            helper.assertTrue(box.equals(LayInNestGoal.target(hen).orElse(null)), "her trip changed to " + LayInNestGoal.target(hen));
            helper.assertTrue(after.next() == 0, "the egg due on the way was used up: " + after);
            helper.succeed();
        });
    }

    // A hen on a lead doesn't go into a box: leashed on her way, she gives up and lays on the ground.
    public static void leashedHenLaysOnTheGround(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.0, 2);
        NestingBoxBlockEntity[] placed = new NestingBoxBlockEntity[1];

        helper.runAfterDelay(LANDED, () -> {
            placed[0] = boxBeside(helper, hen, 5);
            inPen(() -> EggLaying.layDue(hen, helper.getLevel()));
            helper.assertTrue(LayInNestGoal.target(hen).isPresent(), "she didn't set off for the box");
            BlockPos post = hen.blockPosition().north();
            helper.getLevel().setBlockAndUpdate(post, Blocks.OAK_FENCE.defaultBlockState());
            hen.setLeashedTo(LeashFenceKnotEntity.getOrCreateKnot(helper.getLevel(), post), true);
            boolean took = placed[0].enter(hen, new ItemStack(Items.EGG), helper.getLevel().getGameTime());
            helper.assertTrue(!took, "the box took a leashed hen");
        });

        once(helper, () -> eggsNear(helper, hen, Items.EGG) == 1, () -> {
            helper.assertTrue(LayInNestGoal.target(hen).isEmpty() && !placed[0].occupied(), "a leashed hen still went for the box");
            helper.succeed();
        });
    }

    // Another entity with her id (a copied box, /clone) can't keep her in: she comes out under a new id.
    public static void henComesOutDespiteACopy(GameTestHelper helper) {
        NestingBoxBlockEntity box = box(helper, BOX);
        Animal hen = hen(helper, MEDIUM_HEN, 0.5);
        UUID id = hen.getUUID();
        box.enter(hen, new ItemStack(Items.EGG), helper.getLevel().getGameTime());
        //? if >=1.21.2 {
        Animal twin = EntityTypes.CHICKEN.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        //?} else
        //Animal twin = EntityTypes.CHICKEN.create(helper.getLevel());
        twin.setUUID(id);
        twin.setPos(helper.absoluteVec(new Vec3(5.5, 1.0, 6.5)));
        helper.getLevel().addFreshEntity(twin);
        int before = chickensIn(helper);

        box.finishSitting();

        helper.assertTrue(chickensIn(helper) == before + 1, "the hen didn't come out: " + before + " chickens before, " + chickensIn(helper) + " after");
        helper.assertTrue(!box.occupied(), "the hen is still in the box");
        helper.succeed();
    }

    // A fed, familiar hen with a fed rooster and a box with room nearby lays fertilized eggs tomorrow.
    public static void herdBookShowsFertilizedEggs(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 2);
        Animal rooster = chicken(helper, Sex.MALE, 1.2, 0.5, 4);
        FamiliarityCare.markEaten(hen);
        FamiliarityCare.markEaten(rooster);
        box(helper, BOX);

        HerdEntry entry = bookEntry(helper, hen);

        helper.assertTrue(entry.eggs().equals(Optional.of(EggOutlook.Kind.FERTILIZED)), "the book shows " + entry.eggs());
        helper.assertTrue(entry.eggTip().equals(TextComponents.of(new EggOutlook(EggOutlook.Kind.FERTILIZED, 2, List.of()).tooltip())),
                "the icon's tooltip is " + entry.eggTip());
        helper.succeed();
    }

    public static void herdBookSaysWhyEggsArePlain(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 2);
        FamiliarityCare.markEaten(hen);
        box(helper, BOX);

        HerdEntry entry = bookEntry(helper, hen);

        helper.assertTrue(entry.eggs().equals(Optional.of(EggOutlook.Kind.PLAIN)), "the book shows " + entry.eggs());
        helper.assertTrue(entry.lines().stream().anyMatch(line -> line.kind() == HerdPageText.Kind.EGGS), "her page has no eggs line");
        helper.assertTrue(entry.lines().stream().anyMatch(line -> line.text().equals(TextComponents.of(EggOutlook.Reason.NO_ROOSTER.text()))),
                "her page doesn't say there's no rooster");
        helper.succeed();
    }

    public static void herdBookShowsNoEggsForAnUnfedHen(GameTestHelper helper) {
        Animal hen = chicken(helper, Sex.FEMALE, MEDIUM_HEN, 0.5, 2);
        Animal rooster = chicken(helper, Sex.MALE, 1.2, 0.5, 4);
        FamiliarityCare.markEaten(rooster);
        box(helper, BOX);

        HerdEntry entry = bookEntry(helper, hen);

        helper.assertTrue(entry.eggs().equals(Optional.of(EggOutlook.Kind.NO_EGGS)), "the book shows " + entry.eggs());
        helper.succeed();
    }

    public static void herdBookShowsNoEggsForRoosters(GameTestHelper helper) {
        Animal rooster = chicken(helper, Sex.MALE, 1.2, 0.5, 4);
        FamiliarityCare.markEaten(rooster);

        HerdEntry entry = bookEntry(helper, rooster);

        helper.assertTrue(entry.eggs().isEmpty(), "a rooster has an egg icon: " + entry.eggs());
        helper.succeed();
    }

    // The animal's entry in a new herd book, worked out with ranges that stay inside this test's pen.
    static HerdEntry bookEntry(GameTestHelper helper, Animal animal) {
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        List<HerdEntry>[] entries = new List[1];
        inPen(() -> {
            HerdBooks.register(book, animal);
            entries[0] = HerdBooks.entries(helper.getLevel().getServer(), book.get(BarnyardComponents.HERD_BOOK), helper.getLevel().getGameTime());
        });
        return entries[0].stream().filter(entry -> entry.id().equals(animal.getUUID())).findFirst()
                .orElseThrow(() -> GameTestCompat.failure(helper, "the animal isn't in the book"));
    }

    static EggData fertile(GameTestHelper helper, Animal hen) {
        return EggLaying.fertilizedEgg(hen, helper.getLevel()).flatMap(FertilizedEggItem::data)
                .orElseThrow(() -> GameTestCompat.failure(helper, "no fertilized egg"));
    }

    // A box on the floor the hen stands on, east of where she was put in the pen (x 2, z 4), its hole towards her.
    // It goes by her spawn point, not where she has wandered to, so it always lands inside the pen.
    static NestingBoxBlockEntity boxBeside(GameTestHelper helper, Animal hen, int blocks) {
        return boxAt(helper, penFloor(helper, hen, 2 + blocks, 4));
    }

    static BlockPos penFloor(GameTestHelper helper, Animal hen, int x, int z) {
        return helper.absolutePos(new BlockPos(x, 0, z)).atY(hen.getBlockY());
    }

    static NestingBoxBlockEntity boxAt(GameTestHelper helper, BlockPos absolute) {
        helper.getLevel().setBlockAndUpdate(absolute, BarnyardBlocks.NESTING_BOX.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
        return (NestingBoxBlockEntity) helper.getLevel().getBlockEntity(absolute);
    }

    // Runs then once, on the first tick when is true.
    static void once(GameTestHelper helper, BooleanSupplier is, Runnable then) {
        boolean[] done = {false};
        helper.onEachTick(() -> {
            if (!done[0] && is.getAsBoolean()) {
                done[0] = true;
                then.run();
            }
        });
    }

    static Animal chickIn(GameTestHelper helper, BlockPos pos) {
        List<Chicken> chicks = helper.getLevel().getEntitiesOfClass(Chicken.class, new AABB(helper.absolutePos(pos)));
        if (chicks.size() != 1) {
            throw GameTestCompat.failure(helper, "expected one chick at " + pos + ", found " + chicks.size());
        }
        return chicks.getFirst();
    }

    static NestingBoxBlockEntity box(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, BarnyardBlocks.NESTING_BOX.defaultBlockState());
        return (NestingBoxBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    // An egg dropped onto the pen's floor and ticked by hand until it breaks, all within this tick.
    static void throwAndLand(GameTestHelper helper, ItemStack stack) {
        Vec3 at = helper.absoluteVec(new Vec3(4.5, 3.0, 4.5));
        //? if >=1.21.2 {
        ThrownEgg egg = new ThrownEgg(helper.getLevel(), at.x, at.y, at.z, stack);
        //?} else {
        /*ThrownEgg egg = new ThrownEgg(helper.getLevel(), at.x, at.y, at.z);
        egg.setItem(stack);
        *///?}
        egg.setDeltaMovement(0.0, -1.0, 0.0);
        helper.getLevel().addFreshEntity(egg);
        for (int i = 0; i < 40 && !egg.isRemoved(); i++) {
            egg.tick();
        }
    }

    static int chickensIn(GameTestHelper helper) {
        AABB pen = new AABB(helper.absoluteVec(Vec3.ZERO), helper.absoluteVec(new Vec3(9.0, 5.0, 9.0)));
        return helper.getLevel().getEntitiesOfClass(Chicken.class, pen).size();
    }

    static EggData eggData(double motherScale, double fatherScale, long ticks) {
        return new EggData("minecraft:chicken", UUID.randomUUID(), UUID.randomUUID(), motherScale, Growth.wild(), 0.5, 0.5,
                fatherScale, Growth.wild(), 0.5, "", ticks, ticks);
    }

    static Animal hen(GameTestHelper helper, double scale, double familiarity) {
        return chicken(helper, Sex.FEMALE, scale, familiarity, 3);
    }

    // A test chicken has no eggs of her own due today, so only what a test does makes her lay.
    static Animal chicken(GameTestHelper helper, Sex sex, double scale, double familiarity, int x) {
        Animal chicken = GameTestCompat.spawn(helper, EntityTypes.CHICKEN, x, 1, 4);
        Services.DATA.set(chicken, new AnimalTraits(sex, scale, familiarity, -1L));
        Services.DATA.setLaying(chicken, new Laying(GameDay.of(helper.getLevel().getOverworldClockTime()), List.of(), 0));
        return chicken;
    }

    // One tick of the hen's day at the given overworld clock time: hunger is settled first, as the real tick does.
    // No boxes or roosters in reach, so a due egg is laid on the ground at once.
    static Laying tickAt(GameTestHelper helper, Animal hen, long clock) {
        long before = helper.getLevel().getOverworldClockTime();
        BarnyardConfig config = BarnyardConfig.current();
        try {
            GameTestCompat.setOverworldClock(helper, clock);
            FamiliarityCare.settle(hen, clock);
            BarnyardConfig.set(BarnyardConfig.parse("{\"species\": {\"minecraft:chicken\": {\"eggs\": {\"boxRange\": 0, \"roosterRange\": 0}}}}"));
            EggLaying.serverTick(hen, helper.getLevel());
        } finally {
            BarnyardConfig.set(config);
            GameTestCompat.setOverworldClock(helper, before);
        }
        return Services.DATA.getLaying(hen).orElse(Laying.NONE);
    }

    static long afterMidnight(long day) {
        return at(day, 1);
    }

    // The overworld clock time on a day, given in ticks since that day's midnight.
    static long at(long day, int sinceMidnight) {
        return clock(day) - (GameDay.TICKS - 18_000L) + sinceMidnight;
    }

    static int eggsNear(GameTestHelper helper, Animal animal, Item egg) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, animal.getBoundingBox().inflate(4.0)).stream()
                .filter(item -> item.getItem().is(egg))
                .mapToInt(item -> item.getItem().getCount())
                .sum();
    }
}
