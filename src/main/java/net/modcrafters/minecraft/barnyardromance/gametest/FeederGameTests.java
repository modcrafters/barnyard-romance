package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.familiarity.Origin;
import net.modcrafters.minecraft.barnyardromance.familiarity.Upkeep;
import net.modcrafters.minecraft.barnyardromance.feed.FeederBlockEntity;
import net.modcrafters.minecraft.barnyardromance.feed.FeederHerd;
import net.modcrafters.minecraft.barnyardromance.feed.FeederMenu;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
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
// UpkeepGameTests, so ten days can be settled in a tick.
public final class FeederGameTests {
    public static final BlockPos FEEDER = new BlockPos(2, 2, 2);
    // A hopper moves an item every 8 ticks; this is long enough for eight.
    static final int HOPPER_TICKS = 100;
    // Far past the test world's own clock, so the animals' own ticks never settle these days for them.
    private static final long DAY = 10_000L;

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
            new BreedingGameTests.Case("a_cow_below_familiar_gets_no_food", 100, FeederGameTests::aCowBelowFamiliarGetsNoFood)
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
                if (!cows.stream().allMatch(Animal::onGround)) {
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
                if (!cows.stream().allMatch(Animal::onGround)) {
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
                if (cows.stream().allMatch(Animal::onGround)) {
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
        List<Animal> cows = new ArrayList<>();
        for (int[] column : columns) {
            cows.add(GameTestCompat.spawn(helper, EntityTypes.COW, column[0], 1, column[1]));
        }
        EggGameTests.once(helper, () -> cows.stream().allMatch(Animal::onGround), () -> {
            cows.forEach(FeederGameTests::holdStill);
            then.accept(cows);
        });
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
        helper.getLevel().setBlockAndUpdate(absolute, BarnyardBlocks.MANGER.defaultBlockState());
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
