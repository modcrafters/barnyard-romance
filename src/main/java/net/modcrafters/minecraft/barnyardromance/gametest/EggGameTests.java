package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.modcrafters.minecraft.barnyardromance.eggs.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
//? if >=1.21.5 {
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
//?}

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.CareGameTests.clock;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;

// In-game checks for egg layers. Days are driven by setting the overworld clock and ticking the hen by hand,
// then setting the clock back, all within one tick.
public final class EggGameTests {
    // Chickens use the default sizes: females 0.7 to 0.9, so 0.76 is M and 0.9 is XL.
    static final double MEDIUM_HEN = 0.76;
    static final double LARGE_HEN = 0.9;

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("hen_lays_her_daily_eggs_on_the_ground", 20, EggGameTests::henLaysHerDailyEggsOnTheGround),
            new BreedingGameTests.Case("large_happy_hen_plans_three", 20, EggGameTests::largeHappyHenPlansThree),
            new BreedingGameTests.Case("hungry_hen_plans_none", 20, EggGameTests::hungryHenPlansNone),
            new BreedingGameTests.Case("rooster_and_chick_lay_nothing", 20, EggGameTests::roosterAndChickLayNothing),
            new BreedingGameTests.Case("vanilla_egg_timer_is_off", 20, EggGameTests::vanillaEggTimerIsOff),
            new BreedingGameTests.Case("seeds_never_breed_chickens", 20, EggGameTests::seedsNeverBreedChickens),
            new BreedingGameTests.Case("old_chicken_pregnancy_is_cleared", 20, EggGameTests::oldChickenPregnancyIsCleared),
            new BreedingGameTests.Case("excluded_chickens_lay_like_vanilla", 20, EggGameTests::excludedChickensLayLikeVanilla)
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

    static Animal hen(GameTestHelper helper, double scale, double familiarity) {
        return chicken(helper, Sex.FEMALE, scale, familiarity, 3);
    }

    static Animal chicken(GameTestHelper helper, Sex sex, double scale, double familiarity, int x) {
        Animal chicken = GameTestCompat.spawn(helper, EntityTypes.CHICKEN, x, 1, 4);
        Services.DATA.set(chicken, new AnimalTraits(sex, scale, familiarity, -1L));
        return chicken;
    }

    // One tick of the hen's day at the given overworld clock time: hunger is settled first, as the real tick does.
    static Laying tickAt(GameTestHelper helper, Animal hen, long clock) {
        long before = helper.getLevel().getOverworldClockTime();
        try {
            GameTestCompat.setOverworldClock(helper, clock);
            FamiliarityCare.settle(hen, clock);
            EggLaying.serverTick(hen, helper.getLevel());
        } finally {
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
