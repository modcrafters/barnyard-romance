package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.Escapes;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.familiarity.HungerDamage;
import net.modcrafters.minecraft.barnyardromance.familiarity.Origin;
import net.modcrafters.minecraft.barnyardromance.familiarity.Upkeep;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.CareGameTests.clock;

// In-game checks for a lone animal's daily upkeep: what hunger does to its health, when it counts as cared for, and
// how the config's catch-up mode treats days spent away. Days are passed as overworld clock times, as in
// VigourGameTests, so a test can live through a week in one tick.
public final class UpkeepGameTests {
    private static final long DAY = 10L;

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("a_hungry_familiar_cow_loses_half_a_heart", 20, UpkeepGameTests::aHungryFamiliarCowLosesHalfAHeart),
            new BreedingGameTests.Case("hunger_stops_at_min_health", 20, UpkeepGameTests::hungerStopsAtMinHealth),
            new BreedingGameTests.Case("min_health_zero_starves", 20, UpkeepGameTests::minHealthZeroStarves),
            new BreedingGameTests.Case("a_wild_cow_is_never_hurt", 20, UpkeepGameTests::aWildCowIsNeverHurt),
            new BreedingGameTests.Case("one_day_mode_counts_a_week_as_a_day", 20, UpkeepGameTests::oneDayModeCountsAWeekAsADay),
            new BreedingGameTests.Case("none_mode_ignores_missed_days", 20, UpkeepGameTests::noneModeIgnoresMissedDays),
            new BreedingGameTests.Case("reaching_the_threshold_marks_her_cared_for", 20, UpkeepGameTests::reachingTheThresholdMarksHerCaredFor),
            new BreedingGameTests.Case("an_old_save_herd_book_cow_counts_as_cared_for", 20, UpkeepGameTests::anOldSaveHerdBookCowCountsAsCaredFor),
            new BreedingGameTests.Case("a_farm_born_calf_in_a_book_is_not_cared_for", 20, UpkeepGameTests::aFarmBornCalfInABookIsNotCaredFor),
            new BreedingGameTests.Case("hunger_keeps_a_sitting_wolf_sitting", 20, UpkeepGameTests::hungerKeepsASittingWolfSitting),
            new BreedingGameTests.Case("none_mode_still_counts_last_night", 20, UpkeepGameTests::noneModeStillCountsLastNight),
            new BreedingGameTests.Case("hungry_days_stop_hurting_below_familiar", 20, UpkeepGameTests::hungryDaysStopHurtingBelowFamiliar),
            new BreedingGameTests.Case("min_health_clamps_partway", 20, UpkeepGameTests::minHealthClampsPartway),
            new BreedingGameTests.Case("a_penned_runaway_ends_up_outside", 100, UpkeepGameTests::aPennedRunawayEndsUpOutside),
            new BreedingGameTests.Case("a_leaping_runaway_clears_the_fence", 100, UpkeepGameTests::aLeapingRunawayClearsTheFence),
            new BreedingGameTests.Case("a_runaway_never_rolls_again", 20, UpkeepGameTests::aRunawayNeverRollsAgain)
    );

    private UpkeepGameTests() {
    }

    public static void aHungryFamiliarCowLosesHalfAHeart(GameTestHelper helper) {
        Animal cow = cow(helper, 1.0, 10.0F, DAY - 1L);

        FamiliarityCare.settle(cow, clock(DAY));

        assertHealth(helper, cow, 9.0F);
        helper.succeed();
    }

    public static void hungerStopsAtMinHealth(GameTestHelper helper) {
        Animal cow = cow(helper, 1.0, 1.0F, DAY - 3L);

        FamiliarityCare.settle(cow, clock(DAY));

        assertHealth(helper, cow, 1.0F);
        helper.assertTrue(cow.isAlive(), "a cow at the minimum health starved");
        helper.succeed();
    }

    public static void minHealthZeroStarves(GameTestHelper helper) {
        Animal cow = cow(helper, 1.0, 1.0F, DAY - 1L);
        withConfig("{\"hunger\": {\"minHealth\": 0}}", () -> FamiliarityCare.settle(cow, clock(DAY)));

        helper.assertTrue(cow.isDeadOrDying(), "a cow at 1 health with no minimum is still alive at " + cow.getHealth());
        DamageSource source = cow.getLastDamageSource();
        helper.assertTrue(source != null && source.is(HungerDamage.HUNGER), "the cow died of " + source + ", not hunger");
        helper.succeed();
    }

    public static void aWildCowIsNeverHurt(GameTestHelper helper) {
        Animal cow = cow(helper, 0.2, 10.0F, DAY - 3L);

        FamiliarityCare.settle(cow, clock(DAY));

        assertHealth(helper, cow, 10.0F);
        helper.assertTrue(!traits(helper, cow).upkeep().cared(), "a wild cow counts as cared for");
        helper.succeed();
    }

    public static void oneDayModeCountsAWeekAsADay(GameTestHelper helper) {
        Animal cow = cow(helper, 0.8, 10.0F, DAY - 7L);
        withConfig("{\"hunger\": {\"catchUp\": \"ONE_DAY\"}}", () -> FamiliarityCare.settle(cow, clock(DAY)));

        assertHealth(helper, cow, 9.0F);
        assertFamiliarity(helper, cow, 0.75);
        helper.succeed();
    }

    public static void noneModeIgnoresMissedDays(GameTestHelper helper) {
        Animal cow = cow(helper, 0.8, 10.0F, DAY - 7L);
        withConfig("{\"hunger\": {\"catchUp\": \"NONE\"}}", () -> FamiliarityCare.settle(cow, clock(DAY)));

        assertHealth(helper, cow, 10.0F);
        assertFamiliarity(helper, cow, 0.8);
        long settled = traits(helper, cow).hunger().settledDay();
        helper.assertTrue(settled == DAY, "the cow is settled to day " + settled + ", not " + DAY);
        helper.succeed();
    }

    public static void reachingTheThresholdMarksHerCaredFor(GameTestHelper helper) {
        Animal cow = cow(helper, 0.25, 10.0F, DAY - 1L);
        FamiliarityCare.settle(cow, clock(DAY));
        helper.assertTrue(!traits(helper, cow).upkeep().cared(), "a cow at 25% counts as cared for");
        Services.DATA.set(cow, FamiliarityCare.raise(cow, traits(helper, cow), 0.3, -1L));

        FamiliarityCare.settle(cow, clock(DAY + 1L));

        helper.assertTrue(traits(helper, cow).upkeep().cared(), "a cow raised to 30% isn't cared for");
        helper.succeed();
    }

    // A herd book animal from before upkeep was kept is cared for, however far it has fallen since. At 20% her hungry
    // day leaves her above the escape line, so she can't run off and lose it again.
    public static void anOldSaveHerdBookCowCountsAsCaredFor(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        settledOn(cow, traits(helper, cow), 0.2, 10.0F, DAY - 1L, Upkeep.UNKNOWN);

        FamiliarityCare.settle(cow, clock(DAY));

        Upkeep upkeep = traits(helper, cow).upkeep();
        helper.assertTrue(upkeep.cared() && !upkeep.equals(Upkeep.UNKNOWN), "an old save's herd book cow settled to " + upkeep);
        helper.succeed();
    }

    // Only old saves get the herd book shortcut: a calf born into a book this version is cared for once she's familiar.
    public static void aFarmBornCalfInABookIsNotCaredFor(GameTestHelper helper) {
        Animal calf = female(helper, EntityTypes.COW);
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), calf);
        calf.setBaby(true);
        settledOn(calf, traits(helper, calf), 0.1, 10.0F, DAY - 1L);

        FamiliarityCare.settle(calf, clock(DAY));

        helper.assertTrue(!traits(helper, calf).upkeep().cared(), "a calf at 10% in a book counts as cared for");
        helper.succeed();
    }

    public static void hungerKeepsASittingWolfSitting(GameTestHelper helper) {
        TamableAnimal wolf = CareGameTests.tamedWolf(helper, helper.makeMockServerPlayerInLevel(), 1.0);
        settledOn(wolf, traits(helper, wolf), 1.0, 6.0F, DAY - 1L);
        wolf.setOrderedToSit(true);
        wolf.setInSittingPose(true);

        FamiliarityCare.settle(wolf, clock(DAY));

        assertHealth(helper, wolf, 5.0F);
        helper.assertTrue(wolf.isOrderedToSit() && wolf.isInSittingPose(), "a hungry wolf stood up");
        helper.succeed();
    }

    // The catch-up modes are for days spent away: last night always counts.
    public static void noneModeStillCountsLastNight(GameTestHelper helper) {
        Animal cow = cow(helper, 0.8, 10.0F, DAY - 1L);
        withConfig("{\"hunger\": {\"catchUp\": \"NONE\"}}", () -> FamiliarityCare.settle(cow, clock(DAY)));

        assertHealth(helper, cow, 9.0F);
        assertFamiliarity(helper, cow, 0.75);
        helper.succeed();
    }

    // A day hurts while she starts it familiar: 0.40, 0.35 and 0.30 do, 0.25 doesn't.
    public static void hungryDaysStopHurtingBelowFamiliar(GameTestHelper helper) {
        Animal cow = cow(helper, 0.4, 10.0F, DAY - 4L);

        FamiliarityCare.settle(cow, clock(DAY));

        assertHealth(helper, cow, 7.0F);
        assertFamiliarity(helper, cow, 0.2);
        helper.succeed();
    }

    public static void minHealthClampsPartway(GameTestHelper helper) {
        Animal cow = cow(helper, 1.0, 2.0F, DAY - 3L);

        FamiliarityCare.settle(cow, clock(DAY));

        assertHealth(helper, cow, 1.0F);
        helper.succeed();
    }

    // Settling days spent away puts her straight outside, with no one there to see her go.
    public static void aPennedRunawayEndsUpOutside(GameTestHelper helper) {
        runsAway(helper, true, 100L);
    }

    public static void aLeapingRunawayClearsTheFence(GameTestHelper helper) {
        runsAway(helper, false, 60L);
    }

    // Running away clears cared, so a month of hunger at 0% neither hurts her nor sends her off again.
    public static void aRunawayNeverRollsAgain(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        EggGameTests.once(helper, cow::onGround, () -> {
            settledOn(cow, traits(helper, cow), 0.0, 10.0F, DAY, Upkeep.NONE.caredFor().runAway());
            Vec3 at = cow.position();
            Vec3 moving = cow.getDeltaMovement();
            Path path = cow.getNavigation().getPath();

            FamiliarityCare.settle(cow, clock(DAY + 30L));

            assertHealth(helper, cow, 10.0F);
            helper.assertTrue(cow.position().equals(at) && cow.getDeltaMovement().equals(moving) && cow.getNavigation().getPath() == path,
                    "a runaway at 0% set off again");
            Upkeep upkeep = traits(helper, cow).upkeep();
            helper.assertTrue(upkeep.ranAway() && !upkeep.cared(), "a runaway's upkeep is " + upkeep);
            helper.succeed();
        });
    }

    // Once she has landed, fences a neglected cow in and sends her off, then waits for her to stand outside the ring.
    // It's all one tick action: on 1.21.1, adding one from inside another breaks the test runner's loop.
    private static void runsAway(GameTestHelper helper, boolean catchingUp, long within) {
        Animal cow = female(helper, EntityTypes.COW);
        BlockPos[] middle = {null};
        long[] ran = {0L};
        helper.onEachTick(() -> {
            if (middle[0] == null) {
                if (cow.onGround()) {
                    middle[0] = fenceRing(helper, cow);
                    neglected(helper, cow);
                    ran[0] = helper.getTick();

                    Escapes.runAway(cow, helper.getLevel(), catchingUp);

                    Upkeep upkeep = traits(helper, cow).upkeep();
                    helper.assertTrue(upkeep.ranAway() && !upkeep.cared(), "a runaway's upkeep is " + upkeep);
                }
            } else if (cow.onGround() && outside(cow, middle[0])) {
                helper.succeed();
            } else if (helper.getTick() > ran[0] + within) {
                throw GameTestCompat.failure(helper, "the cow is still in the pen at " + helper.relativePos(cow.blockPosition())
                        + ", the pen's middle at " + helper.relativePos(middle[0]));
            }
        });
    }

    // A one-high ring of fence two blocks out from the animal's block, so it stands in the middle of a 3x3 pen. On
    // 1.21.1 the test is boxed in with a barrier lid three blocks up, too low to leap under, so it's lifted off.
    private static BlockPos fenceRing(GameTestHelper helper, Animal animal) {
        BlockPos middle = animal.blockPosition();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == 2) {
                    helper.getLevel().setBlockAndUpdate(middle.offset(dx, 0, dz), Blocks.OAK_FENCE.defaultBlockState());
                }
                for (int dy = 2; dy <= 4; dy++) {
                    BlockPos above = middle.offset(dx, dy, dz);
                    if (helper.getLevel().getBlockState(above).is(Blocks.BARRIER)) {
                        helper.getLevel().setBlockAndUpdate(above, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        return middle;
    }

    // Cared for once, now down to 8%.
    private static void neglected(GameTestHelper helper, Animal animal) {
        Services.DATA.set(animal, traits(helper, animal).fed(0.08, -1L).withOrigin(Origin.FARM).withUpkeep(Upkeep.NONE.caredFor()));
    }

    private static boolean outside(Animal animal, BlockPos middle) {
        BlockPos at = animal.blockPosition();
        return Math.max(Math.abs(at.getX() - middle.getX()), Math.abs(at.getZ() - middle.getZ())) > 2;
    }

    private static Animal cow(GameTestHelper helper, double familiarity, float health, long settledDay) {
        Animal cow = female(helper, EntityTypes.COW);
        return settledOn(cow, traits(helper, cow), familiarity, health, settledDay);
    }

    // Farm-raised, at the given familiarity and health, last settled on settledDay and unfed since.
    private static Animal settledOn(Animal animal, AnimalTraits traits, double familiarity, float health, long settledDay) {
        return settledOn(animal, traits, familiarity, health, settledDay, Upkeep.NONE);
    }

    private static Animal settledOn(Animal animal, AnimalTraits traits, double familiarity, float health, long settledDay, Upkeep upkeep) {
        Services.DATA.set(animal, traits.fed(familiarity, -1L).withOrigin(Origin.FARM)
                .withHunger(new Hunger(settledDay, -1L, -1L)).withUpkeep(upkeep));
        animal.setHealth(health);
        return animal;
    }

    // Swaps the config for the length of one call, so no other test sees it.
    private static void withConfig(String json, Runnable body) {
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse(json));
            body.run();
        } finally {
            BarnyardConfig.set(config);
        }
    }

    private static void assertHealth(GameTestHelper helper, Animal animal, float expected) {
        helper.assertTrue(Math.abs(animal.getHealth() - expected) < 1.0e-4F, "health is " + animal.getHealth() + ", expected " + expected);
    }

    private static void assertFamiliarity(GameTestHelper helper, Animal animal, double expected) {
        double actual = traits(helper, animal).familiarity();
        helper.assertTrue(Math.abs(actual - expected) < 1.0e-6, "familiarity is " + actual + ", expected " + expected);
    }
}
