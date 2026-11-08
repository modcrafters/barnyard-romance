package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.eggs.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.familiarity.Origin;
import net.modcrafters.minecraft.barnyardromance.familiarity.Upkeep;
import net.modcrafters.minecraft.barnyardromance.feed.EatAtFeederGoal;
import net.modcrafters.minecraft.barnyardromance.feed.FeederBlockEntity;
import net.modcrafters.minecraft.barnyardromance.feed.FeederHerd;
import net.modcrafters.minecraft.barnyardromance.feed.FeederMenu;
import net.modcrafters.minecraft.barnyardromance.feed.MorningFeeding;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBookData;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.herd.HerdEntry;
import net.modcrafters.minecraft.barnyardromance.herd.HerdRecord;
import net.modcrafters.minecraft.barnyardromance.herd.HerdStatus;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.mixin.MobGoalsAccessor;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.stats.LiveFacts;
import net.modcrafters.minecraft.barnyardromance.stats.StatKind;
import net.modcrafters.minecraft.barnyardromance.stats.StatSheet;
import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.modcrafters.minecraft.barnyardromance.gametest.CareGameTests.clock;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;

// In-game checks for the feeding blocks: what each takes, by hand and by hopper, and that nothing takes the food
// back out (the Manger's grazers eat seeds, so beef is the plain food it turns away); then which animals make a
// block's herd, and how the herd's missed days are settled. Days are passed as overworld clock times, as in
// UpkeepGameTests, so ten days can be settled in a tick. The morning tests, where the herd walks over to eat, run
// the block on a clock of their own (runOnClock): the overworld's is shared with the tests beside them.
public final class FeederGameTests {
    public static final BlockPos FEEDER = new BlockPos(2, 2, 2);
    // A hopper moves an item every 8 ticks; this is long enough for eight.
    static final int HOPPER_TICKS = 100;
    // Far past the test world's own clock, so the animals' own ticks never settle these days for them.
    private static final long DAY = 10_000L;
    private static final long SUNRISE = clock(DAY);

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("feeder_takes_wheat_but_not_beef", 20, FeederGameTests::feederTakesWheatButNotBeef),
            new BreedingGameTests.Case("feeder_refuses_superfoods", 20, FeederGameTests::feederRefusesSuperfoods),
            new BreedingGameTests.Case("bird_feeder_takes_seeds", 20, FeederGameTests::birdFeederTakesSeeds),
            new BreedingGameTests.Case("a_hopper_fills_a_manger_and_keeps_what_it_refuses", HOPPER_TICKS + 40,
                    FeederGameTests::aHopperFillsAMangerAndKeepsWhatItRefuses),
            new BreedingGameTests.Case("a_hopper_cannot_empty_a_manger", HOPPER_TICKS + 40, FeederGameTests::aHopperCannotEmptyAManger),
            new BreedingGameTests.Case("breaking_a_manger_drops_its_food", 20, FeederGameTests::breakingAMangerDropsItsFood),
            new BreedingGameTests.Case("a_meal_comes_from_the_first_full_slot", 20, FeederGameTests::aMealComesFromTheFirstFullSlot),
            new BreedingGameTests.Case("a_manger_finds_the_cows_in_its_pen", 100, FeederGameTests::aMangerFindsTheCowsInItsPen),
            new BreedingGameTests.Case("a_cow_in_a_pen_corner_stays_in_the_herd", 100, FeederGameTests::aCowInAPenCornerStaysInTheHerd),
            new BreedingGameTests.Case("an_unfamiliar_cow_is_not_in_the_herd", 100, FeederGameTests::anUnfamiliarCowIsNotInTheHerd),
            new BreedingGameTests.Case("catch_up_shares_two_days_of_food_over_ten_days", 100,
                    FeederGameTests::catchUpSharesTwoDaysOfFoodOverTenDays),
            new BreedingGameTests.Case("a_cow_waits_for_its_manger", 140, FeederGameTests::aCowWaitsForItsManger),
            new BreedingGameTests.Case("breaking_the_manger_mid_catch_up_lets_the_rest_settle_alone", 240,
                    FeederGameTests::breakingTheMangerMidCatchUpLetsTheRestSettleAlone),
            new BreedingGameTests.Case("catch_up_does_not_walk", 100, FeederGameTests::catchUpDoesNotWalk),
            new BreedingGameTests.Case("a_cow_in_two_herds_is_settled_once", 100, FeederGameTests::aCowInTwoHerdsIsSettledOnce),
            new BreedingGameTests.Case("none_mode_catch_up_takes_no_food", 100, FeederGameTests::noneModeCatchUpTakesNoFood),
            new BreedingGameTests.Case("one_day_mode_catch_up_feeds_one_day", 100, FeederGameTests::oneDayModeCatchUpFeedsOneDay),
            new BreedingGameTests.Case("two_mangers_catching_up_at_once_settle_each_day_once", 100,
                    FeederGameTests::twoMangersCatchingUpAtOnceSettleEachDayOnce),
            new BreedingGameTests.Case("a_day_the_pen_was_loaded_takes_no_food", 100, FeederGameTests::aDayThePenWasLoadedTakesNoFood),
            new BreedingGameTests.Case("a_cow_below_familiar_gets_no_food", 100, FeederGameTests::aCowBelowFamiliarGetsNoFood),
            new BreedingGameTests.Case("a_herd_hen_hungry_yesterday_lays_nothing", 100, FeederGameTests::aHerdHenHungryYesterdayLaysNothing),
            new BreedingGameTests.Case("cows_walk_to_the_manger_and_eat", 400, FeederGameTests::cowsWalkToTheMangerAndEat),
            new BreedingGameTests.Case("no_more_than_four_walk_at_once", 800, FeederGameTests::noMoreThanFourWalkAtOnce),
            new BreedingGameTests.Case("a_hand_fed_cow_stays_put", 400, FeederGameTests::aHandFedCowStaysPut),
            new BreedingGameTests.Case("a_feeder_meal_is_not_love", 400, FeederGameTests::aFeederMealIsNotLove),
            new BreedingGameTests.Case("food_added_at_noon_feeds_the_hungry", 400, FeederGameTests::foodAddedAtNoonFeedsTheHungry),
            new BreedingGameTests.Case("a_sitting_wolf_is_not_moved", 300, FeederGameTests::aSittingWolfIsNotMoved),
            new BreedingGameTests.Case("a_catch_up_meal_heals", 100, FeederGameTests::aCatchUpMealHeals),
            new BreedingGameTests.Case("a_cow_in_a_corner_walks_out_to_eat", 400, FeederGameTests::aCowInACornerWalksOutToEat),
            new BreedingGameTests.Case("a_manger_meal_shows_on_the_fed_row", 400, FeederGameTests::aMangerMealShowsOnTheFedRow),
            new BreedingGameTests.Case("a_brief_morning_load_leaves_the_rest_of_the_day_to_catch_up", 400,
                    FeederGameTests::aBriefMorningLoadLeavesTheRestOfTheDayToCatchUp),
            new BreedingGameTests.Case("feeding_block_recipes_craft_their_blocks", 20, FeederGameTests::feedingBlockRecipesCraftTheirBlocks)
    );

    private FeederGameTests() {
    }

    // By hand too: the window's slots ask the block.
    public static void feederTakesWheatButNotBeef(GameTestHelper helper) {
        FeederBlockEntity manger = feeder(helper, BarnyardBlocks.MANGER, FEEDER);
        Player player = holding(helper, Items.BEEF);
        FeederMenu menu = new FeederMenu(1, player.getInventory(), manger);

        helper.assertTrue(manger.canPlaceItem(0, new ItemStack(Items.WHEAT)), "the Manger refuses wheat");
        helper.assertTrue(!manger.canPlaceItem(0, new ItemStack(Items.BEEF)), "the Manger takes beef");
        helper.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(Items.WHEAT)), "the Manger's window refuses wheat");
        helper.assertTrue(!menu.getSlot(0).mayPlace(new ItemStack(Items.BEEF)), "the Manger's window takes beef");
        helper.succeed();
    }

    public static void feederRefusesSuperfoods(GameTestHelper helper) {
        FeederBlockEntity manger = feeder(helper, BarnyardBlocks.MANGER, FEEDER);

        helper.assertTrue(!manger.canPlaceItem(0, new ItemStack(BarnyardItems.STUD_MASH)), "the Manger takes Stud Mash");
        helper.assertTrue(!manger.canPlaceItem(0, new ItemStack(BarnyardItems.BREEDERS_MASH)), "the Manger takes Breeder's Mash");
        helper.succeed();
    }

    public static void birdFeederTakesSeeds(GameTestHelper helper) {
        FeederBlockEntity birds = feeder(helper, BarnyardBlocks.BIRD_FEEDER, FEEDER);

        helper.assertTrue(birds.canPlaceItem(0, new ItemStack(Items.WHEAT_SEEDS)), "the Bird Feeder refuses seeds");
        helper.assertTrue(!birds.canPlaceItem(0, new ItemStack(Items.BEEF)), "the Bird Feeder takes beef");
        helper.succeed();
    }

    // The refused beef sits first in the hopper, so the hopper has to pass it over to move the wheat.
    public static void aHopperFillsAMangerAndKeepsWhatItRefuses(GameTestHelper helper) {
        BlockPos below = FEEDER.below();
        FeederBlockEntity manger = feeder(helper, BarnyardBlocks.MANGER, below);
        helper.setBlock(FEEDER, Blocks.HOPPER.defaultBlockState());
        HopperBlockEntity hopper = (HopperBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(FEEDER));
        hopper.setItem(0, new ItemStack(Items.BEEF, 4));
        hopper.setItem(1, new ItemStack(Items.WHEAT, 4));

        helper.runAfterDelay(HOPPER_TICKS, () -> {
            helper.assertTrue(manger.food() == 4 && count(manger, Items.WHEAT) == 4, "the Manger holds " + contents(manger));
            helper.assertTrue(count(hopper, Items.BEEF) == 4 && count(hopper, Items.WHEAT) == 0, "the hopper holds " + contents(hopper));
            helper.succeed();
        });
    }

    public static void aHopperCannotEmptyAManger(GameTestHelper helper) {
        FeederBlockEntity manger = feeder(helper, BarnyardBlocks.MANGER, FEEDER);
        manger.setItem(0, new ItemStack(Items.WHEAT, 4));
        helper.setBlock(FEEDER.below(), Blocks.HOPPER.defaultBlockState());
        HopperBlockEntity hopper = (HopperBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(FEEDER.below()));

        helper.runAfterDelay(HOPPER_TICKS, () -> {
            helper.assertTrue(count(manger, Items.WHEAT) == 4, "the Manger holds " + contents(manger));
            helper.assertTrue(hopper.isEmpty(), "the hopper took " + contents(hopper));
            helper.succeed();
        });
    }

    public static void breakingAMangerDropsItsFood(GameTestHelper helper) {
        FeederBlockEntity manger = feeder(helper, BarnyardBlocks.MANGER, FEEDER);
        manger.setItem(0, new ItemStack(Items.WHEAT, 5));
        manger.setItem(4, new ItemStack(Items.HAY_BLOCK, 2));

        // As a player breaks it, so its loot table drops the block too.
        helper.getLevel().destroyBlock(helper.absolutePos(FEEDER), true);

        List<ItemEntity> dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(FEEDER)).inflate(2.0));
        helper.assertTrue(dropped(dropped, Items.WHEAT) == 5, "breaking the Manger dropped " + dropped(dropped, Items.WHEAT) + " wheat");
        helper.assertTrue(dropped(dropped, Items.HAY_BLOCK) == 2, "breaking the Manger dropped " + dropped(dropped, Items.HAY_BLOCK) + " hay");
        helper.assertTrue(dropped(dropped, BarnyardBlocks.MANGER_ITEM) == 1, "breaking the Manger didn't drop the Manger");
        helper.succeed();
    }

    public static void aMealComesFromTheFirstFullSlot(GameTestHelper helper) {
        FeederBlockEntity manger = feeder(helper, BarnyardBlocks.MANGER, FEEDER);
        manger.setItem(2, new ItemStack(Items.WHEAT, 1));
        manger.setItem(5, new ItemStack(Items.CARROT, 3));

        ItemStack first = manger.takeOne(helper.getLevel());
        ItemStack second = manger.takeOne(helper.getLevel());
        int left = manger.food();
        manger.takeOne(helper.getLevel());
        manger.takeOne(helper.getLevel());
        ItemStack empty = manger.takeOne(helper.getLevel());

        helper.assertTrue(first.is(Items.WHEAT) && first.getCount() == 1, "the first meal was " + first);
        helper.assertTrue(second.is(Items.CARROT) && second.getCount() == 1, "the second meal was " + second);
        helper.assertTrue(left == 2 && manger.getItem(2).isEmpty(), "after two meals the Manger holds " + contents(manger));
        helper.assertTrue(empty.isEmpty() && manger.food() == 0, "an empty Manger gave " + empty);
        helper.succeed();
    }

    // A fence ring keeps the fourth cow out of the pen, though she's well within reach.
    public static void aMangerFindsTheCowsInItsPen(GameTestHelper helper) {
        onceLanded(helper, new int[][]{{1, 3}, {3, 3}, {1, 5}, {6, 4}}, cows -> {
            cows.forEach(cow -> familiar(cow, 0.8));
            BlockPos middle = walkingLayer(helper, cows.getFirst(), 2, 4);
            fenceRing(helper, middle);
            FeederBlockEntity manger = mangerAt(helper, middle);

            manger.refreshHerd(helper.getLevel());

            assertHerd(helper, manger, cows.subList(0, 3));
            for (int i = 0; i < cows.size(); i++) {
                boolean inHerd = BreedingGameTests.traits(helper, cows.get(i)).upkeep().inHerd();
                helper.assertTrue(inHerd == i < 3, "cow " + i + (inHerd ? " is" : " isn't") + " marked as in a herd");
            }
            helper.succeed();
        });
    }

    // A path search from some spots of a pen's corner, between two fence lines, never leaves its start, which says
    // nothing about whether the animal can get out. Two cows (one scaled up) and a horse are found for a Manger by the
    // pen's side, then pressed into three corners and a cow put in the fourth: the Manger keeps them all, though the
    // far corners are more than 4 blocks off. A new Manger in the middle, within 4 blocks of every corner, takes them all
    // too.
    public static void aCowInAPenCornerStaysInTheHerd(GameTestHelper helper) {
        List<Animal> animals = List.of(GameTestCompat.spawn(helper, EntityTypes.COW, 5, 1, 5), GameTestCompat.spawn(helper, EntityTypes.COW, 3, 1, 5),
                GameTestCompat.spawn(helper, EntityTypes.COW, 3, 1, 3), GameTestCompat.spawn(helper, EntityTypes.HORSE, 5, 1, 3));
        // Which way each is pressed into its corner: towards the far fence (1) or the near one (-1).
        int[][] corners = {{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
        EggGameTests.once(helper, () -> ready(helper, animals), () -> {
            animals.forEach(FeederGameTests::holdStill);
            penIn(helper, animals.getFirst());
            Services.DATA.set(animals.get(1), new AnimalTraits(Sex.FEMALE, 1.3, 0.8, -1L).withOrigin(Origin.FARM));
            animals.get(1).getAttribute(Attributes.SCALE).setBaseValue(1.3);
            animals.get(1).refreshDimensions();
            familiar(animals.get(0), 0.8);
            familiar(animals.get(2), 0.8);
            familiar(animals.get(3), 0.8);
            FeederBlockEntity side = mangerAt(helper, walkingLayer(helper, animals.getFirst(), 1, 4));
            side.refreshHerd(helper.getLevel());
            assertHerd(helper, side, animals);

            for (int i = 0; i < animals.size(); i++) {
                Animal animal = animals.get(i);
                // Up against the fence posts on both sides, which stand 0.375 in from the pen's edge.
                double half = animal.getBbWidth() / 2.0 + 0.05;
                double x = corners[i][0] > 0 ? 8.375 - half : 0.625 + half;
                double z = corners[i][1] > 0 ? 8.375 - half : 0.625 + half;
                // The first stands where a cow wandering in the pen was seen to find no path, on both versions.
                if (i == 0) {
                    x = 7.69;
                    z = 7.70;
                }
                Vec3 corner = helper.absoluteVec(new Vec3(x, 0.0, z));
                animal.teleportTo(corner.x, animal.getY(), corner.z);
            }
            side.refreshHerd(helper.getLevel());
            assertHerd(helper, side, animals);

            FeederBlockEntity middle = mangerAt(helper, walkingLayer(helper, animals.getFirst(), 4, 4));
            middle.refreshHerd(helper.getLevel());
            assertHerd(helper, middle, animals);
            helper.succeed();
        });
    }

    public static void anUnfamiliarCowIsNotInTheHerd(GameTestHelper helper) {
        onceLanded(helper, new int[][]{{3, 4}, {5, 4}}, cows -> {
            familiar(cows.get(0), 0.8);
            familiar(cows.get(1), 0.2);
            FeederBlockEntity manger = mangerAt(helper, walkingLayer(helper, cows.getFirst(), 4, 4));

            manger.refreshHerd(helper.getLevel());

            assertHerd(helper, manger, cows.subList(0, 1));
            helper.succeed();
        });
    }

    // Food for two days of ten: every cow eats on the first two and goes hungry on the other eight.
    public static void catchUpSharesTwoDaysOfFoodOverTenDays(GameTestHelper helper) {
        threeCowsAndAManger(helper, (cows, manger) -> {
            FeederMenu menu = new FeederMenu(1, helper.makeMockPlayer(GameType.SURVIVAL).getInventory(), manger);
            helper.assertTrue(menu.animals() == 3 && menu.days() == 2, "the window says " + menu.days() + " days for " + menu.animals() + " animals");

            manger.tick(helper.getLevel(), clock(DAY));

            helper.assertTrue(manger.settledDay() == DAY, "the Manger is settled to day " + manger.settledDay() + ", not " + DAY);
            helper.assertTrue(manger.food() == 0, "the Manger still holds " + contents(manger));
            for (Animal cow : cows) {
                assertSettled(helper, cow, 2.0F, 0.4);
                AnimalTraits traits = BreedingGameTests.traits(helper, cow);
                helper.assertTrue(traits.hunger().lastAteDay() == DAY - 9L, "the cow last ate on day " + traits.hunger().lastAteDay());
                helper.assertTrue(traits.hunger().lastHungryDay() == DAY - 1L, "the cow was last hungry on day " + traits.hunger().lastHungryDay());
                helper.assertTrue(traits.upkeep().fedByFeederOn(DAY - 9L) && traits.upkeep().feeder().equals("manger"),
                        "the cow's upkeep is " + traits.upkeep());
            }
            helper.succeed();
        });
    }

    // With no feeder loaded, a cow in a herd gives hers a moment to load, then settles alone.
    public static void aCowWaitsForItsManger(GameTestHelper helper) {
        Animal cow = GameTestCompat.spawn(helper, EntityTypes.COW, 4, 1, 4);
        unsettled(cow, 0.8, DAY - 3L, Upkeep.NONE.withInHerd(true));
        helper.onEachTick(() -> {
            FamiliarityCare.settle(cow, clock(DAY));
            long settled = BreedingGameTests.traits(helper, cow).hunger().settledDay();
            long tick = helper.getTick();
            if (tick <= 98L) {
                helper.assertTrue(settled == DAY - 3L, "the cow settled alone after only " + tick + " ticks");
            } else if (tick >= 101L) {
                helper.assertTrue(settled == DAY, "the cow is settled to day " + settled + " on tick " + tick);
                assertSettled(helper, cow, 7.0F, 0.65);
                helper.succeed();
            }
        });
    }

    // 20 cows and 15 days is more than one tick's work. Food lasts 9 days, so each cow goes hungry on 6 whether the
    // Manger or the cow settles them. One that was settled twice would have lost more.
    public static void breakingTheMangerMidCatchUpLetsTheRestSettleAlone(GameTestHelper helper) {
        List<Animal> cows = twentyCows(helper);
        long[] broken = {-1L};
        helper.onEachTick(() -> {
            if (broken[0] < 0L) {
                if (!ready(helper, cows)) {
                    return;
                }
                cows.forEach(FeederGameTests::holdStill);
                BlockPos at = walkingLayer(helper, cows.getFirst(), 4, 4);
                FeederBlockEntity manger = stockedFor20(helper, cows, at);

                manger.tick(helper.getLevel(), clock(DAY));

                helper.assertTrue(manger.settledDay() < DAY, "300 cow-days were settled in one tick");
                helper.assertTrue(cows.stream().anyMatch(cow -> settledDay(helper, cow) > DAY - 15L), "the Manger's first tick settled nothing");
                helper.getLevel().destroyBlock(at, false);
                broken[0] = helper.getTick();
                return;
            }
            cows.forEach(cow -> FamiliarityCare.settle(cow, clock(DAY)));
            if (cows.stream().allMatch(cow -> settledDay(helper, cow) == DAY)) {
                cows.forEach(cow -> assertSettled(helper, cow, 4.0F, 0.5));
                helper.succeed();
            } else if (helper.getTick() > broken[0] + 120L) {
                throw GameTestCompat.failure(helper, "cows still unsettled 120 ticks after the Manger broke: "
                        + cows.stream().map(cow -> settledDay(helper, cow)).toList());
            }
        });
    }

    // A second Manger starts on the same 20 cows while the first is partway through. Each day is settled by one of
    // them only: the first gives up a cow the second has moved on.
    public static void twoMangersCatchingUpAtOnceSettleEachDayOnce(GameTestHelper helper) {
        List<Animal> cows = twentyCows(helper);
        FeederBlockEntity[] first = {null};
        helper.onEachTick(() -> {
            if (first[0] == null) {
                if (!ready(helper, cows)) {
                    return;
                }
                cows.forEach(FeederGameTests::holdStill);
                first[0] = stockedFor20(helper, cows, walkingLayer(helper, cows.getFirst(), 4, 4));
                FeederBlockEntity second = mangerAt(helper, walkingLayer(helper, cows.getFirst(), 4, 7));
                second.refreshHerd(helper.getLevel());
                assertHerd(helper, second, cows);

                first[0].tick(helper.getLevel(), clock(DAY));
                helper.assertTrue(first[0].settledDay() < DAY, "300 cow-days were settled in one tick");
                second.tick(helper.getLevel(), clock(DAY));
                helper.assertTrue(second.settledDay() == DAY, "the second Manger is settled to day " + second.settledDay());
                return;
            }
            if (first[0].settledDay() == DAY) {
                cows.forEach(cow -> assertSettled(helper, cow, 4.0F, 0.5));
                helper.succeed();
            }
        });
    }

    // Last night the pen was loaded, so the herd had its chance to walk over and eat. A cow that didn't isn't fed from
    // the Manger at midnight.
    public static void aDayThePenWasLoadedTakesNoFood(GameTestHelper helper) {
        onceLanded(helper, new int[][]{{3, 4}}, cows -> {
            Animal cow = cows.getFirst();
            FeederBlockEntity manger = mangerAt(helper, walkingLayer(helper, cow, 4, 4));
            manger.setItem(0, new ItemStack(Items.WHEAT, 6));
            unsettled(cow, 0.8, DAY - 1L, Upkeep.NONE);
            manger.refreshHerd(helper.getLevel());
            manger.tick(helper.getLevel(), clock(DAY - 1L));

            manger.tick(helper.getLevel(), clock(DAY));

            helper.assertTrue(manger.food() == 6, "the Manger holds " + contents(manger));
            assertSettled(helper, cow, 9.0F, 0.75);
            helper.succeed();
        });
    }

    // Her familiarity has fallen under the threshold since the herd was found, so she gets nothing; the other cow eats
    // every day. Never familiar since she was marked, she is neither hurt nor sent off.
    public static void aCowBelowFamiliarGetsNoFood(GameTestHelper helper) {
        List<Animal> cows = List.of(GameTestCompat.spawn(helper, EntityTypes.COW, 3, 1, 4), GameTestCompat.spawn(helper, EntityTypes.COW, 5, 1, 4));
        FeederBlockEntity[] manger = {null};
        helper.onEachTick(() -> {
            if (manger[0] == null) {
                if (ready(helper, cows)) {
                    cows.forEach(FeederGameTests::holdStill);
                    manger[0] = mangerAt(helper, walkingLayer(helper, cows.getFirst(), 4, 4));
                    manger[0].setItem(0, new ItemStack(Items.WHEAT, 20));
                    cows.forEach(cow -> unsettled(cow, 0.8, DAY - 10L, Upkeep.NONE));
                    // A tick just before sunrise on her first day: the Manger finds its herd, as a block just placed
                    // does, and has nothing to settle yet.
                    manger[0].tick(helper.getLevel(), clock(DAY - 10L) - 1L);
                    assertHerd(helper, manger[0], cows);
                }
                return;
            }
            // Not on the Manger's own tick of the minute, when it would find its herd again without her.
            if (FeederHerd.due(helper.getLevel().getGameTime(), manger[0].refreshSlot())) {
                return;
            }
            unsettled(cows.get(1), 0.25, DAY - 10L, Upkeep.NONE.withInHerd(true));

            manger[0].tick(helper.getLevel(), clock(DAY));

            helper.assertTrue(manger[0].food() == 10, "the Manger holds " + contents(manger[0]));
            assertSettled(helper, cows.get(0), 10.0F, 0.8);
            assertSettled(helper, cows.get(1), 10.0F, 0.0);
            helper.succeed();
        });
    }

    // Her day is planned from yesterday's hunger, so it waits until her Bird Feeder has settled yesterday for her. It
    // held no food, so she went hungry and lays nothing today; planned from her unsettled day she'd lay a clutch.
    public static void aHerdHenHungryYesterdayLaysNothing(GameTestHelper helper) {
        onceLanded(helper, EntityTypes.CHICKEN, new int[][]{{3, 4}}, hens -> {
            Animal hen = hens.getFirst();
            FeederBlockEntity feeder = blockAt(helper, BarnyardBlocks.BIRD_FEEDER, walkingLayer(helper, hen, 4, 4));
            unsettled(hen, 0.8, DAY - 1L, Upkeep.NONE);
            // A tick just before her day, as a block placed earlier would have had: it finds its herd and has nothing
            // to settle yet.
            feeder.tick(helper.getLevel(), clock(DAY - 1L) - 1L);
            assertHerd(helper, feeder, hens);
            long morning = EggGameTests.at(DAY, 7_000);

            Laying waiting = EggGameTests.tickAt(helper, hen, morning);
            helper.assertTrue(waiting.day() != DAY, "the hen planned her day before it was settled: " + waiting);
            feeder.tick(helper.getLevel(), morning);
            Laying planned = EggGameTests.tickAt(helper, hen, morning);

            helper.assertTrue(planned.day() == DAY && planned.planned() == 0, "the hungry hen planned " + planned);
            helper.succeed();
        });
    }

    // Her first missed day she eats the Manger's one wheat, which heals her as it would by hand (1, the default for a
    // food with no nutrition), and the second she goes hungry (1 damage): back where she started. Unhealed, she'd be at 5.
    public static void aCatchUpMealHeals(GameTestHelper helper) {
        onceLanded(helper, new int[][]{{3, 4}}, cows -> {
            Animal cow = cows.getFirst();
            FeederBlockEntity manger = mangerAt(helper, walkingLayer(helper, cow, 4, 4));
            manger.setItem(0, new ItemStack(Items.WHEAT, 1));
            unsettled(cow, 0.8, DAY - 2L, Upkeep.NONE);
            cow.setHealth(6.0F);
            manger.refreshHerd(helper.getLevel());

            manger.tick(helper.getLevel(), clock(DAY));

            helper.assertTrue(manger.food() == 0, "the Manger holds " + contents(manger));
            assertSettled(helper, cow, 6.0F, 0.75);
            helper.succeed();
        });
    }

    // The food just leaves the block: nobody is sent anywhere.
    public static void catchUpDoesNotWalk(GameTestHelper helper) {
        threeCowsAndAManger(helper, (cows, manger) -> {
            List<Vec3> before = cows.stream().map(Animal::position).toList();

            manger.tick(helper.getLevel(), clock(DAY));

            for (int i = 0; i < cows.size(); i++) {
                Animal cow = cows.get(i);
                helper.assertTrue(cow.getNavigation().isDone(), "cow " + i + " was sent walking");
                helper.assertTrue(cow.position().equals(before.get(i)), "cow " + i + " moved during catch-up");
            }
            helper.succeed();
        });
    }

    // Whichever Manger gets to her first settles her missed days; the other leaves her be.
    public static void aCowInTwoHerdsIsSettledOnce(GameTestHelper helper) {
        onceLanded(helper, new int[][]{{4, 4}}, cows -> {
            Animal cow = cows.getFirst();
            unsettled(cow, 0.8, DAY - 2L, Upkeep.NONE);
            FeederBlockEntity first = mangerAt(helper, walkingLayer(helper, cow, 2, 4));
            FeederBlockEntity second = mangerAt(helper, walkingLayer(helper, cow, 6, 4));
            first.setItem(0, new ItemStack(Items.WHEAT, 3));
            second.setItem(0, new ItemStack(Items.WHEAT, 3));
            first.refreshHerd(helper.getLevel());
            second.refreshHerd(helper.getLevel());
            assertHerd(helper, first, cows);
            assertHerd(helper, second, cows);

            first.tick(helper.getLevel(), clock(DAY));
            second.tick(helper.getLevel(), clock(DAY));

            helper.assertTrue(first.food() == 1 && second.food() == 3, "the Mangers hold " + first.food() + " and " + second.food());
            helper.assertTrue(second.settledDay() == DAY, "the second Manger is settled to day " + second.settledDay());
            assertSettled(helper, cow, 10.0F, 0.8);
            helper.succeed();
        });
    }

    public static void noneModeCatchUpTakesNoFood(GameTestHelper helper) {
        threeCowsAndAManger(helper, (cows, manger) -> {
            UpkeepGameTests.withConfig("{\"hunger\": {\"catchUp\": \"NONE\"}}", () -> manger.tick(helper.getLevel(), clock(DAY)));

            helper.assertTrue(manger.food() == 6, "the Manger holds " + contents(manger));
            cows.forEach(cow -> assertSettled(helper, cow, 10.0F, 0.8));
            helper.succeed();
        });
    }

    // Ten days away count as one, and that day comes out of the Manger.
    public static void oneDayModeCatchUpFeedsOneDay(GameTestHelper helper) {
        threeCowsAndAManger(helper, (cows, manger) -> {
            UpkeepGameTests.withConfig("{\"hunger\": {\"catchUp\": \"ONE_DAY\"}}", () -> manger.tick(helper.getLevel(), clock(DAY)));

            helper.assertTrue(manger.food() == 3, "the Manger holds " + contents(manger));
            for (Animal cow : cows) {
                assertSettled(helper, cow, 10.0F, 0.8);
                long ate = BreedingGameTests.traits(helper, cow).hunger().lastAteDay();
                helper.assertTrue(ate == DAY - 1L, "the cow last ate on day " + ate);
            }
            helper.succeed();
        });
    }

    // Three cows six blocks off walk over at sunrise and eat the Manger's three wheat, one each. None sets off before
    // sunrise.
    public static void cowsWalkToTheMangerAndEat(GameTestHelper helper) {
        Set<Animal> near = new HashSet<>();
        morning(helper, EntityTypes.COW, new int[][]{{7, 3}, {7, 4}, {7, 5}}, BarnyardBlocks.MANGER, new int[]{1, 4},
                m -> m.feeder.setItem(0, new ItemStack(Items.WHEAT, 3)),
                m -> {
                    for (Animal cow : m.animals) {
                        helper.assertTrue(m.clock >= SUNRISE || !EatAtFeederGoal.walking(cow), "a cow set off before sunrise");
                        if (EatAtFeederGoal.within(cow, m.at, 2.0)) {
                            near.add(cow);
                        }
                    }
                    if (m.animals.stream().allMatch(cow -> ateOn(helper, cow, DAY)) && m.feeder.food() == 0 && near.size() == 3) {
                        for (Animal cow : m.animals) {
                            Upkeep upkeep = BreedingGameTests.traits(helper, cow).upkeep();
                            helper.assertTrue(upkeep.fedByFeederOn(DAY) && upkeep.feeder().equals("manger"), "the cow's upkeep is " + upkeep);
                        }
                        helper.succeed();
                    }
                });
    }

    // Ten hungry cows and ten wheat: they go four at a time, and in the end all of them eat.
    public static void noMoreThanFourWalkAtOnce(GameTestHelper helper) {
        int[] most = {0};
        // Away from the pen's corners. A search from a corner may never leave it, and a cow there more than 4 blocks
        // from the Manger only joins its herd at a later refresh (see HerdRefresh.find), after this test is over.
        int[][] columns = {{4, 2}, {4, 6}, {5, 2}, {5, 4}, {5, 6}, {6, 3}, {6, 5}, {7, 2}, {7, 4}, {7, 6}};
        morning(helper, EntityTypes.COW, columns, BarnyardBlocks.MANGER, new int[]{2, 4},
                m -> m.feeder.setItem(0, new ItemStack(Items.WHEAT, 10)),
                m -> {
                    int walking = (int) m.animals.stream().filter(FeederGameTests::goalRunning).count();
                    helper.assertTrue(walking <= MorningFeeding.MAX_WALKING, walking + " cows are walking to the Manger at once");
                    most[0] = Math.max(most[0], walking);
                    if (m.animals.stream().allMatch(cow -> ateOn(helper, cow, DAY)) && m.feeder.food() == 0) {
                        helper.assertTrue(most[0] == MorningFeeding.MAX_WALKING, "at most " + most[0] + " cows ever walked at once");
                        helper.succeed();
                    }
                });
    }

    // Fed by hand before sunrise, she's had today's meal, so she's never sent and the Manger keeps its second wheat.
    // The other cow eats at the Manger, which leaves her hand-feeding to come: it still wins her over a little.
    public static void aHandFedCowStaysPut(GameTestHelper helper) {
        long[] otherAte = {-1L};
        morning(helper, EntityTypes.COW, new int[][]{{7, 3}, {7, 5}}, BarnyardBlocks.MANGER, new int[]{1, 4},
                m -> {
                    m.feeder.setItem(0, new ItemStack(Items.WHEAT, 2));
                    handFeed(helper, m.animals.get(0), m.clock);
                    helper.assertTrue(ateOn(helper, m.animals.get(0), DAY), "the hand-fed cow hasn't eaten today");
                },
                m -> {
                    Animal handFed = m.animals.get(0);
                    Animal other = m.animals.get(1);
                    helper.assertTrue(!EatAtFeederGoal.walking(handFed) && !goalRunning(handFed), "the hand-fed cow was sent to the Manger");
                    if (otherAte[0] < 0L) {
                        if (ateOn(helper, other, DAY)) {
                            otherAte[0] = helper.getTick();
                        }
                        return;
                    }
                    // Five more rounds for the hand-fed cow to be sent, if she were going to be.
                    if (helper.getTick() < otherAte[0] + 5L * MorningFeeding.ROUND_TICKS) {
                        return;
                    }
                    helper.assertTrue(m.feeder.food() == 1, "the Manger holds " + contents(m.feeder));
                    double before = BreedingGameTests.traits(helper, other).familiarity();
                    handFeed(helper, other, m.clock);
                    double after = BreedingGameTests.traits(helper, other).familiarity();
                    helper.assertTrue(after > before + 1.0e-9, "hand-feeding after her Manger meal left her at " + after);
                    helper.succeed();
                });
    }

    // Her wheat at the Manger heals her, but doesn't win her over or put her in love as the same wheat would by hand.
    public static void aFeederMealIsNotLove(GameTestHelper helper) {
        morning(helper, EntityTypes.COW, new int[][]{{7, 4}}, BarnyardBlocks.MANGER, new int[]{1, 4},
                m -> {
                    m.feeder.setItem(0, new ItemStack(Items.WHEAT, 1));
                    m.animals.getFirst().setHealth(6.0F);
                },
                m -> {
                    Animal cow = m.animals.getFirst();
                    if (!ateOn(helper, cow, DAY)) {
                        return;
                    }
                    AnimalTraits traits = BreedingGameTests.traits(helper, cow);
                    helper.assertTrue(m.feeder.food() == 0, "the Manger holds " + contents(m.feeder));
                    helper.assertTrue(!cow.isInLove(), "a Manger meal put her in love");
                    helper.assertTrue(Math.abs(traits.familiarity() - 0.8) < 1.0e-9, "a Manger meal moved her familiarity to " + traits.familiarity());
                    helper.assertTrue(traits.lastFedTick() == -1L, "a Manger meal counted as hand-feeding at " + traits.lastFedTick());
                    helper.assertTrue(cow.getHealth() > 6.0F, "a Manger meal left her health at " + cow.getHealth());
                    helper.succeed();
                });
    }

    // The Manger is empty at sunrise, so nobody walks. At noon it gets wheat, and the cows are fed by the next rounds.
    public static void foodAddedAtNoonFeedsTheHungry(GameTestHelper helper) {
        long[] stocked = {-1L};
        morning(helper, EntityTypes.COW, new int[][]{{7, 3}, {7, 4}, {7, 5}}, BarnyardBlocks.MANGER, new int[]{1, 4},
                m -> {
                },
                m -> {
                    if (stocked[0] < 0L) {
                        m.animals.forEach(cow -> helper.assertTrue(!EatAtFeederGoal.walking(cow), "a cow walked to the empty Manger"));
                        if (m.clock >= SUNRISE + 60L) {
                            m.clock = SUNRISE + 6_000L;
                            m.feeder.setItem(0, new ItemStack(Items.WHEAT, 3));
                            stocked[0] = helper.getTick();
                        }
                        return;
                    }
                    if (m.animals.stream().allMatch(cow -> ateOn(helper, cow, DAY))) {
                        helper.assertTrue(m.feeder.food() == 0, "the Manger holds " + contents(m.feeder));
                        helper.succeed();
                    } else if (helper.getTick() > stocked[0] + 200L) {
                        throw GameTestCompat.failure(helper, "the cows hadn't all eaten 200 ticks after the Manger was filled");
                    }
                });
    }

    // Told to sit five blocks from her Pet Bowl, she stays sitting there and goes without. Moved to two blocks off,
    // still sitting, she eats from it where she sits.
    public static void aSittingWolfIsNotMoved(GameTestHelper helper) {
        Vec3[] seat = {null};
        long[] moved = {-1L};
        morning(helper, EntityTypes.WOLF, new int[][]{{7, 4}}, BarnyardBlocks.PET_BOWL, new int[]{2, 4},
                m -> {
                    TamableAnimal wolf = (TamableAnimal) m.animals.getFirst();
                    wolf.tame(helper.makeMockPlayer(GameType.SURVIVAL));
                    wolf.setOrderedToSit(true);
                    wolf.setInSittingPose(true);
                    m.feeder.setItem(0, new ItemStack(Items.BEEF, 2));
                    seat[0] = wolf.position();
                },
                m -> {
                    TamableAnimal wolf = (TamableAnimal) m.animals.getFirst();
                    helper.assertTrue(wolf.isOrderedToSit() && wolf.isInSittingPose(), "the wolf got up");
                    helper.assertTrue(wolf.position().distanceTo(seat[0]) < 0.1, "the sitting wolf moved from " + seat[0] + " to " + wolf.position());
                    helper.assertTrue(!EatAtFeederGoal.walking(wolf), "the sitting wolf was sent to the Pet Bowl");
                    if (moved[0] < 0L) {
                        helper.assertTrue(!ateOn(helper, wolf, DAY), "the wolf ate from five blocks off");
                        if (m.clock >= SUNRISE + 5L * MorningFeeding.ROUND_TICKS) {
                            BlockPos two = m.at.offset(2, 0, 0);
                            wolf.teleportTo(two.getX() + 0.5, wolf.getY(), two.getZ() + 0.5);
                            seat[0] = wolf.position();
                            moved[0] = helper.getTick();
                        }
                        return;
                    }
                    if (ateOn(helper, wolf, DAY)) {
                        helper.assertTrue(m.feeder.food() == 1, "the Pet Bowl holds " + contents(m.feeder));
                        helper.succeed();
                    } else if (helper.getTick() > moved[0] + 3L * MorningFeeding.ROUND_TICKS) {
                        throw GameTestCompat.failure(helper, "the wolf two blocks off hadn't eaten");
                    }
                });
    }

    // From the corner spot where a path search may never leave its start, she still gets to the Manger and eats.
    public static void aCowInACornerWalksOutToEat(GameTestHelper helper) {
        morning(helper, EntityTypes.COW, new int[][]{{7, 7}}, BarnyardBlocks.MANGER, new int[]{3, 4},
                m -> {
                    m.feeder.setItem(0, new ItemStack(Items.WHEAT, 1));
                    Animal cow = m.animals.getFirst();
                    Vec3 corner = helper.absoluteVec(new Vec3(7.69, 0.0, 7.70));
                    cow.teleportTo(corner.x, cow.getY(), corner.z);
                },
                m -> {
                    if (ateOn(helper, m.animals.getFirst(), DAY)) {
                        helper.assertTrue(m.feeder.food() == 0, "the Manger holds " + contents(m.feeder));
                        helper.succeed();
                    }
                });
    }

    // Fed at the Manger and not by hand, she is fed today on her stat sheet and her herd book page, which name the
    // Manger. The book's contents icon is the half drumstick, and its tooltip names the Manger too.
    public static void aMangerMealShowsOnTheFedRow(GameTestHelper helper) {
        morning(helper, EntityTypes.COW, new int[][]{{7, 4}}, BarnyardBlocks.MANGER, new int[]{1, 4},
                m -> m.feeder.setItem(0, new ItemStack(Items.WHEAT, 1)),
                m -> {
                    Animal cow = m.animals.getFirst();
                    if (!ateOn(helper, cow, DAY)) {
                        return;
                    }
                    long now = helper.getLevel().getGameTime();
                    String live = StatSheet.lines(LiveFacts.of(cow, now, m.clock).orElseThrow()).stream()
                            .filter(line -> line.kind() == StatKind.FED).findFirst()
                            .map(line -> TextComponents.of(line.text()).getString()).orElse("missing");
                    helper.assertTrue(live.equals("Fed today (Manger)"), "her fed row reads " + live);
                    HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
                    HerdBookData data = HerdBookData.get(helper.getLevel().getServer());
                    HerdRecord record = data.record(cow.getUUID());
                    for (boolean loaded : new boolean[]{true, false}) {
                        HerdEntry entry = HerdStatus.entry(record, data.ledger().node(cow.getUUID()), now, m.clock, loaded);
                        String page = entry.lines().stream().filter(line -> line.kind() == StatKind.FED).findFirst()
                                .map(line -> line.text().getString()).orElse("missing");
                        String how = loaded ? "" : " while unloaded";
                        helper.assertTrue(page.equals("Fed today (Manger)"), "her page's fed row reads " + page + how);
                        helper.assertTrue(entry.fed() == HerdEntry.Fed.FEEDER && entry.feeder().equals("manger"),
                                "the contents page shows her " + entry.fed() + " by '" + entry.feeder() + "'" + how);
                    }
                    helper.succeed();
                });
    }

    // The pen is loaded just after sunrise, long enough for the nearest cow to walk over and eat, then unloaded for
    // three days. The other two never had their chance that morning, so the Manger feeds them that day when the pen
    // loads again, and the food it holds lasts the whole herd the same: all three ate on the first two days and went
    // hungry on the third.
    public static void aBriefMorningLoadLeavesTheRestOfTheDayToCatchUp(GameTestHelper helper) {
        FeederBlockEntity[] reloaded = {null};
        morning(helper, EntityTypes.COW, new int[][]{{3, 4}, {7, 3}, {7, 5}}, BarnyardBlocks.MANGER, new int[]{1, 4},
                m -> m.feeder.setItem(0, new ItemStack(Items.WHEAT, 6)),
                m -> {
                    if (reloaded[0] == null) {
                        long ate = m.animals.stream().filter(cow -> ateOn(helper, cow, DAY)).count();
                        if (ate == 0L) {
                            return;
                        }
                        helper.assertTrue(ate < m.animals.size(), "every cow ate before the pen could unload");
                        m.animals.forEach(FeederGameTests::holdStill);
                        // Unloaded: saved and taken out of the level, so nothing ticks it. It comes back three days on,
                        // as from a save, and settles the days it missed.
                        BlockPos pos = m.at;
                        CompoundTag saved = m.feeder.saveWithFullMetadata(helper.getLevel().registryAccess());
                        helper.getLevel().removeBlockEntity(pos);
                        m.clock += 3L * 24_000L;
                        reloaded[0] = (FeederBlockEntity) BlockEntity.loadStatic(pos, helper.getLevel().getBlockState(pos), saved,
                                helper.getLevel().registryAccess());
                        helper.getLevel().setBlockEntity(reloaded[0]);
                        reloaded[0].runOnClock(helper.getLevel(), m.clock);
                        return;
                    }
                    if (reloaded[0].settledDay() < DAY + 3L) {
                        return;
                    }
                    helper.assertTrue(reloaded[0].food() == 0, "the Manger holds " + contents(reloaded[0]));
                    for (Animal cow : m.animals) {
                        AnimalTraits traits = BreedingGameTests.traits(helper, cow);
                        helper.assertTrue(traits.hunger().settledDay() == DAY + 3L, "a cow is settled to day " + traits.hunger().settledDay());
                        helper.assertTrue(traits.hunger().lastAteDay() == DAY + 1L && traits.hunger().lastHungryDay() == DAY + 2L,
                                "a cow last ate on day " + traits.hunger().lastAteDay() + " and was last hungry on " + traits.hunger().lastHungryDay());
                        // One hungry day each: one that missed the brief morning would have lost twice this.
                        helper.assertTrue(Math.abs(traits.familiarity() - 0.75) < 1.0e-6, "a cow's familiarity is " + traits.familiarity());
                        helper.assertTrue(Math.abs(cow.getHealth() - 9.0F) < 1.0e-4F, "a cow's health is " + cow.getHealth());
                    }
                    helper.succeed();
                });
    }

    // A morning test: its animals, its feeding block and the clock the block runs on.
    private static final class Morning {
        final List<Animal> animals;
        final FeederBlockEntity feeder;
        final BlockPos at;
        long clock = SUNRISE - 20L;

        Morning(List<Animal> animals, FeederBlockEntity feeder, BlockPos at) {
            this.animals = animals;
            this.feeder = feeder;
            this.at = at;
        }
    }

    // Landed animals, fenced in round the edge of the floor, farm-raised and familiar (80%) at 10 health and settled
    // up to today (DAY), around a feeding block at this column. The block runs on the test's own clock, which starts
    // 20 ticks before sunrise and moves on a tick each tick; a test can move it further.
    private static void morning(GameTestHelper helper, EntityType<? extends Animal> type, int[][] columns, Block block, int[] at,
                                Consumer<Morning> setUp, Consumer<Morning> each) {
        List<Animal> animals = new ArrayList<>();
        for (int[] column : columns) {
            animals.add(GameTestCompat.spawn(helper, type, column[0], 1, column[1]));
        }
        Morning[] morning = {null};
        helper.onEachTick(() -> {
            if (morning[0] == null) {
                if (!ready(helper, animals)) {
                    return;
                }
                penIn(helper, animals.getFirst());
                // Back to where they started, in case one wandered to the edge and is now in the fence.
                for (int i = 0; i < animals.size(); i++) {
                    BlockPos start = walkingLayer(helper, animals.get(i), columns[i][0], columns[i][1]);
                    animals.get(i).teleportTo(start.getX() + 0.5, animals.get(i).getY(), start.getZ() + 0.5);
                }
                animals.forEach(animal -> unsettled(animal, 0.8, DAY, Upkeep.NONE));
                BlockPos pos = walkingLayer(helper, animals.getFirst(), at[0], at[1]);
                morning[0] = new Morning(animals, blockAt(helper, block, pos), pos);
                morning[0].feeder.runOnClock(helper.getLevel(), morning[0].clock);
                setUp.accept(morning[0]);
                return;
            }
            Morning m = morning[0];
            m.clock++;
            each.accept(m);
            m.feeder.runOnClock(helper.getLevel(), m.clock);
        });
    }

    // A fence round the edge of the floor, on the level a landed animal stands on, with nothing above it: the 1.21.1
    // pen's glass walls would otherwise hang over the fence, where an animal pressed into a corner sticks in them.
    private static void penIn(GameTestHelper helper, Animal landed) {
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                if (x == 0 || x == 8 || z == 0 || z == 8) {
                    BlockPos edge = walkingLayer(helper, landed, x, z);
                    helper.getLevel().setBlockAndUpdate(edge.above(), Blocks.AIR.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(edge.above(2), Blocks.AIR.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(edge, Blocks.OAK_FENCE.defaultBlockState());
                }
            }
        }
    }

    // By hand, on the test's clock: the overworld's is set to it for the feed and put back.
    private static void handFeed(GameTestHelper helper, Animal animal, long clock) {
        long before = helper.getLevel().getOverworldClockTime();
        try {
            GameTestCompat.setOverworldClock(helper, clock);
            FamiliarityActions.interact(animal, holding(helper, Items.WHEAT), InteractionHand.MAIN_HAND);
        } finally {
            GameTestCompat.setOverworldClock(helper, before);
        }
    }

    private static boolean ateOn(GameTestHelper helper, Animal animal, long day) {
        return BreedingGameTests.traits(helper, animal).hunger().lastAteDay() == day;
    }

    // The animal's walk to a feeding block is under way: its goal is running.
    private static boolean goalRunning(Animal animal) {
        return ((MobGoalsAccessor) animal).barnyardromance$goalSelector().getAvailableGoals().stream()
                .anyMatch(goal -> goal.isRunning() && goal.getGoal() instanceof EatAtFeederGoal);
    }

    // Three familiar cows at 10 health and 80%, ten days unsettled, around a Manger holding 6 wheat.
    private static void threeCowsAndAManger(GameTestHelper helper, BiConsumer<List<Animal>, FeederBlockEntity> then) {
        onceLanded(helper, new int[][]{{3, 4}, {5, 4}, {4, 6}}, cows -> {
            FeederBlockEntity manger = mangerAt(helper, walkingLayer(helper, cows.getFirst(), 4, 4));
            manger.setItem(0, new ItemStack(Items.WHEAT, 6));
            cows.forEach(cow -> unsettled(cow, 0.8, DAY - 10L, Upkeep.NONE));
            manger.refreshHerd(helper.getLevel());
            assertHerd(helper, manger, cows);
            then.accept(cows, manger);
        });
    }

    // Packed a block apart around the middle, away from the pen's edges, where on 1.21.1 some cows found no path.
    private static List<Animal> twentyCows(GameTestHelper helper) {
        List<Animal> cows = new ArrayList<>();
        for (int x = 2; x <= 6; x++) {
            for (int z = 2; z <= 6; z++) {
                if ((x != 4 || z != 4) && cows.size() < 20) {
                    cows.add(GameTestCompat.spawn(helper, EntityTypes.COW, x, 1, z));
                }
            }
        }
        return cows;
    }

    // The 20 cows, 15 days unsettled at 80% and 10 health, around a Manger holding 180 wheat: food for 9 days.
    private static FeederBlockEntity stockedFor20(GameTestHelper helper, List<Animal> cows, BlockPos at) {
        FeederBlockEntity manger = mangerAt(helper, at);
        manger.setItem(0, new ItemStack(Items.WHEAT, 64));
        manger.setItem(1, new ItemStack(Items.WHEAT, 64));
        manger.setItem(2, new ItemStack(Items.WHEAT, 52));
        cows.forEach(cow -> unsettled(cow, 0.8, DAY - 15L, Upkeep.NONE));
        manger.refreshHerd(helper.getLevel());
        assertHerd(helper, manger, cows);
        return manger;
    }

    // Cows spawned on these columns. Once all have landed they're held still, so only the test moves them.
    private static void onceLanded(GameTestHelper helper, int[][] columns, Consumer<List<Animal>> then) {
        onceLanded(helper, EntityTypes.COW, columns, then);
    }

    private static void onceLanded(GameTestHelper helper, EntityType<? extends Animal> type, int[][] columns, Consumer<List<Animal>> then) {
        List<Animal> cows = new ArrayList<>();
        for (int[] column : columns) {
            cows.add(GameTestCompat.spawn(helper, type, column[0], 1, column[1]));
        }
        EggGameTests.once(helper, () -> ready(helper, cows), () -> {
            cows.forEach(FeederGameTests::holdStill);
            then.accept(cows);
        });
    }

    // The animals have landed, and the animals around the pen are loaded, as a block waits for before it settles
    // anything: entities load apart from blocks, and a test near the edge of the loaded area may wait a few ticks.
    private static boolean ready(GameTestHelper helper, List<Animal> animals) {
        return animals.stream().allMatch(Animal::onGround)
                && FeederBlockEntity.animalsLoaded(helper.getLevel(), helper.absolutePos(new BlockPos(0, 0, 0)))
                && FeederBlockEntity.animalsLoaded(helper.getLevel(), helper.absolutePos(new BlockPos(8, 0, 8)));
    }

    // A mob with no AI never lands, so this waits until it has.
    private static void holdStill(Animal animal) {
        animal.setNoAi(true);
        animal.getNavigation().stop();
    }

    // The block at this column of the pen on the level a landed animal stands on.
    private static BlockPos walkingLayer(GameTestHelper helper, Animal landed, int x, int z) {
        return helper.absolutePos(new BlockPos(x, 0, z)).atY(landed.blockPosition().getY());
    }

    private static FeederBlockEntity mangerAt(GameTestHelper helper, BlockPos absolute) {
        return blockAt(helper, BarnyardBlocks.MANGER, absolute);
    }

    private static FeederBlockEntity blockAt(GameTestHelper helper, Block block, BlockPos absolute) {
        helper.getLevel().setBlockAndUpdate(absolute, block.defaultBlockState());
        return (FeederBlockEntity) helper.getLevel().getBlockEntity(absolute);
    }

    // A one-high fence ring two blocks out from the middle: a 3x3 pen.
    private static void fenceRing(GameTestHelper helper, BlockPos middle) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == 2) {
                    helper.getLevel().setBlockAndUpdate(middle.offset(dx, 0, dz), Blocks.OAK_FENCE.defaultBlockState());
                }
            }
        }
    }

    // Farm-raised and familiar.
    private static void familiar(Animal animal, double familiarity) {
        Services.DATA.set(animal, new AnimalTraits(Sex.FEMALE, 1.0, familiarity, -1L).withOrigin(Origin.FARM));
    }

    // Farm-raised, at this familiarity and 10 health, settled up to the start of day `from` and unfed since.
    private static void unsettled(Animal animal, double familiarity, long from, Upkeep upkeep) {
        Services.DATA.set(animal, new AnimalTraits(Sex.FEMALE, 1.0, familiarity, -1L).withOrigin(Origin.FARM)
                .withHunger(new Hunger(from, -1L, -1L)).withUpkeep(upkeep));
        animal.setHealth(10.0F);
    }

    private static long settledDay(GameTestHelper helper, Animal animal) {
        return BreedingGameTests.traits(helper, animal).hunger().settledDay();
    }

    private static void assertHerd(GameTestHelper helper, FeederBlockEntity feeder, List<Animal> expected) {
        Set<UUID> herd = Set.copyOf(feeder.herd());
        Set<UUID> ids = expected.stream().map(Animal::getUUID).collect(Collectors.toSet());
        helper.assertTrue(herd.equals(ids) && feeder.herd().size() == ids.size(),
                "the herd holds " + feeder.herd().size() + " animals, expected " + ids.size() + (herd.equals(ids) ? "" : " others"));
    }

    private static void assertSettled(GameTestHelper helper, Animal animal, float health, double familiarity) {
        AnimalTraits traits = BreedingGameTests.traits(helper, animal);
        helper.assertTrue(traits.hunger().settledDay() == DAY, "the cow is settled to day " + traits.hunger().settledDay() + ", not " + DAY);
        helper.assertTrue(Math.abs(animal.getHealth() - health) < 1.0e-4F, "the cow's health is " + animal.getHealth() + ", expected " + health);
        helper.assertTrue(Math.abs(traits.familiarity() - familiarity) < 1.0e-6,
                "the cow's familiarity is " + traits.familiarity() + ", expected " + familiarity);
    }

    public static FeederBlockEntity feeder(GameTestHelper helper, Block block, BlockPos pos) {
        helper.setBlock(pos, block.defaultBlockState());
        return (FeederBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static int count(Container container, Item item) {
        int count = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (container.getItem(slot).is(item)) {
                count += container.getItem(slot).getCount();
            }
        }
        return count;
    }

    // Each grid has to make its own block and not a vanilla one: five planks in a U is a boat and three bricks in a
    // V a flower pot, which is why the Trough takes slabs and the Pet Bowl a full row of bricks.
    public static void feedingBlockRecipesCraftTheirBlocks(GameTestHelper helper) {
        Item e = Items.AIR, p = Items.OAK_PLANKS, s = Items.STICK, h = Items.HAY_BLOCK, b = Items.BRICK;
        Item w = Items.WHEAT_SEEDS, l = Items.OAK_SLAB;
        crafts(helper, "bird_feeder", e, s, e, e, w, e, p, p, p);
        crafts(helper, "trough", l, e, l, l, l, l);
        crafts(helper, "feeder", p, e, p, p, h, p, p, p, p);
        crafts(helper, "manger", s, s, s, s, h, s, p, s, p);
        crafts(helper, "pet_bowl", b, e, b, b, b, b);
        helper.succeed();
    }

    // Six items are a 3x2 grid, nine a 3x3 one.
    private static void crafts(GameTestHelper helper, String name, Item... grid) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : grid) {
            stacks.add(new ItemStack(item));
        }
        CraftingInput input = CraftingInput.of(3, grid.length / 3, stacks);
        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        // The holder's id is a ResourceKey on 26.3 and a plain id on 1.21.1; both print the recipe's name.
        helper.assertTrue(recipe.isPresent() && recipe.get().id().toString().contains("barnyard_romance:" + name),
                "the " + name + " grid makes " + recipe.map(holder -> holder.id().toString()).orElse("nothing"));
    }

    private static int dropped(List<ItemEntity> dropped, Item item) {
        return dropped.stream().filter(entity -> entity.getItem().is(item)).mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    private static String contents(Container container) {
        StringBuilder text = new StringBuilder("[");
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (!container.getItem(slot).isEmpty()) {
                text.append(text.length() > 1 ? ", " : "").append(container.getItem(slot));
            }
        }
        return text.append("]").toString();
    }
}
