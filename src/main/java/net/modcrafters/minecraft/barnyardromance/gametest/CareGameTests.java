package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBookData;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.herd.HerdEntry;
import net.modcrafters.minecraft.barnyardromance.herd.HerdPageText;
import net.modcrafters.minecraft.barnyardromance.herd.HerdRecord;
import net.modcrafters.minecraft.barnyardromance.herd.HerdStatus;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.assertFamiliarity;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;

// In-game checks for looking after animals: hunger, familiarity costs, and grumpy pets.
// Days are driven by passing overworld clock times, so a test can live through a week in one tick.
public final class CareGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("fed_cow_keeps_familiarity_over_days", 20, CareGameTests::fedCowKeepsFamiliarityOverDays),
            new BreedingGameTests.Case("hungry_cow_loses_five_percent_a_day", 20, CareGameTests::hungryCowLosesFivePercentADay),
            new BreedingGameTests.Case("feeding_marks_the_day_eaten", 20, CareGameTests::feedingMarksTheDayEaten),
            new BreedingGameTests.Case("superfood_counts_as_eating", 20, CareGameTests::superfoodCountsAsEating),
            new BreedingGameTests.Case("calf_does_not_go_hungry", 20, CareGameTests::calfDoesNotGoHungry),
            new BreedingGameTests.Case("old_tamed_wolf_starts_at_threshold", 20, CareGameTests::oldTamedWolfStartsAtThreshold),
            new BreedingGameTests.Case("horse_tamed_by_riding_is_not_grumpy", 20, CareGameTests::horseTamedByRidingIsNotGrumpy),
            new BreedingGameTests.Case("unloaded_member_shows_projected_familiarity", 20, CareGameTests::unloadedMemberShowsProjectedFamiliarity),
            new BreedingGameTests.Case("milking_costs_familiarity", 20, CareGameTests::milkingCostsFamiliarity),
            new BreedingGameTests.Case("large_cow_milked_twice_costs_ten", 20, CareGameTests::largeCowMilkedTwiceCostsTen),
            new BreedingGameTests.Case("refused_milking_costs_nothing", 20, CareGameTests::refusedMilkingCostsNothing),
            new BreedingGameTests.Case("shearing_costs_familiarity", 20, CareGameTests::shearingCostsFamiliarity),
            new BreedingGameTests.Case("dispenser_shearing_costs_familiarity", 20, CareGameTests::dispenserShearingCostsFamiliarity),
            new BreedingGameTests.Case("mooshroom_stew_costs_familiarity", 20, CareGameTests::mooshroomStewCostsFamiliarity),
            new BreedingGameTests.Case("hungry_cow_gives_no_milk_next_day", 20, CareGameTests::hungryCowGivesNoMilkNextDay),
            new BreedingGameTests.Case("hungry_day_does_not_dry_her_up", 20, CareGameTests::hungryDayDoesNotDryHerUp)
    );

    private CareGameTests() {
    }

    public static void fedCowKeepsFamiliarityOverDays(GameTestHelper helper) {
        Animal cow = withFamiliarity(helper, female(helper, EntityTypes.COW), 0.8);
        FamiliarityCare.settle(cow, clock(10L));

        for (long day = 10L; day < 15L; day++) {
            FamiliarityCare.markEaten(cow, clock(day) + 10_000L);
            FamiliarityCare.settle(cow, clock(day + 1L));
        }

        assertFamiliarity(helper, cow, 0.8);
        helper.succeed();
    }

    public static void hungryCowLosesFivePercentADay(GameTestHelper helper) {
        Animal cow = withFamiliarity(helper, female(helper, EntityTypes.COW), 0.8);
        FamiliarityCare.settle(cow, clock(10L));

        FamiliarityCare.settle(cow, clock(13L));

        assertFamiliarity(helper, cow, 0.65);
        helper.succeed();
    }

    public static void feedingMarksTheDayEaten(GameTestHelper helper) {
        Animal cow = withFamiliarity(helper, female(helper, EntityTypes.COW), 0.4);

        FamiliarityActions.interact(cow, holding(helper, Items.WHEAT), InteractionHand.MAIN_HAND);

        long today = GameDay.of(helper.getLevel().getOverworldClockTime());
        helper.assertTrue(traits(helper, cow).hunger().lastAteDay() == today, "feeding left the last meal on day " + traits(helper, cow).hunger().lastAteDay());
        helper.succeed();
    }

    // Superfoods skip the once-a-day feed, but a mother who ate one hasn't gone hungry.
    public static void superfoodCountsAsEating(GameTestHelper helper) {
        Animal cow = FoodGameTests.pregnantCow(helper, 0.5);

        FamiliarityActions.interact(cow, holding(helper, BarnyardItems.BREEDERS_MASH), InteractionHand.MAIN_HAND);

        long today = GameDay.of(helper.getLevel().getOverworldClockTime());
        helper.assertTrue(traits(helper, cow).hunger().lastAteDay() == today, "the mash left the last meal on day " + traits(helper, cow).hunger().lastAteDay());
        helper.succeed();
    }

    public static void calfDoesNotGoHungry(GameTestHelper helper) {
        Animal calf = withFamiliarity(helper, female(helper, EntityTypes.COW), 0.4);
        calf.setBaby(true);
        FamiliarityCare.settle(calf, clock(10L));

        FamiliarityCare.settle(calf, clock(13L));

        assertFamiliarity(helper, calf, 0.4);
        helper.succeed();
    }

    // A pet tamed before hunger was kept starts at the threshold, so it isn't grumpy the moment this ships.
    public static void oldTamedWolfStartsAtThreshold(GameTestHelper helper) {
        TamableAnimal wolf = (TamableAnimal) female(helper, EntityTypes.WOLF);
        wolf.tame(helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL));
        Services.DATA.set(wolf, traits(helper, wolf).fed(0.0, -1L).withHunger(Hunger.NONE));

        FamiliarityCare.settle(wolf, clock(10L));

        assertFamiliarity(helper, wolf, 0.3);
        helper.assertTrue(!FamiliarityCare.grumpy(wolf), "an old tamed wolf is grumpy");
        helper.succeed();
    }

    public static void horseTamedByRidingIsNotGrumpy(GameTestHelper helper) {
        AbstractHorse horse = (AbstractHorse) withFamiliarity(helper, female(helper, EntityTypes.HORSE), 0.0);
        Player player = holding(helper, Items.AIR);

        horse.tameWithName(player);

        helper.assertTrue(traits(helper, horse).familiarity() >= 0.3, "a horse tamed by riding has familiarity " + traits(helper, horse).familiarity());
        helper.assertTrue(!FamiliarityCare.grumpy(horse), "a horse tamed by riding is grumpy");
        helper.succeed();
    }

    // Far from home, the book still shows the familiarity a hungry animal has lost since it was last seen.
    public static void unloadedMemberShowsProjectedFamiliarity(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        Services.DATA.set(cow, traits(helper, cow).fed(0.5, -1L).withHunger(new Hunger(10L, 10L, -1L)));
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        HerdBooks.leftLevel(cow, Entity.RemovalReason.UNLOADED_TO_CHUNK);
        HerdBookData data = HerdBookData.get(helper.getLevel().getServer());
        HerdRecord record = data.record(cow.getUUID());

        HerdEntry entry = HerdStatus.entry(record, data.ledger().node(cow.getUUID()), helper.getLevel().getGameTime(), clock(14L), false);

        float fill = entry.lines().stream().filter(line -> line.kind() == HerdPageText.Kind.FAMILIARITY).findFirst().orElseThrow().fill();
        helper.assertTrue(Math.abs(fill - 0.35F) < 1.0e-4F, "the book shows familiarity " + fill + ", not 0.35");
        helper.succeed();
    }

    public static void milkingCostsFamiliarity(GameTestHelper helper) {
        Animal cow = withFamiliarity(helper, ProductGameTests.mother(helper, EntityTypes.COW, 0.5), 0.8);

        ProductGameTests.assertMilks(helper, cow, Items.MILK_BUCKET);

        assertFamiliarity(helper, cow, 0.75);
        helper.succeed();
    }

    public static void largeCowMilkedTwiceCostsTen(GameTestHelper helper) {
        Animal cow = withFamiliarity(helper, ProductGameTests.mother(helper, EntityTypes.COW, 0.7), 0.8);

        ProductGameTests.assertMilks(helper, cow, Items.MILK_BUCKET);
        ProductGameTests.assertMilks(helper, cow, Items.MILK_BUCKET);

        assertFamiliarity(helper, cow, 0.7);
        helper.succeed();
    }

    public static void refusedMilkingCostsNothing(GameTestHelper helper) {
        Animal cow = withFamiliarity(helper, ProductGameTests.mother(helper, EntityTypes.COW, 0.0), 0.8);
        ProductGameTests.assertMilks(helper, cow, Items.MILK_BUCKET);
        Player second = holding(helper, Items.BUCKET);

        GameTestCompat.interact(cow, second);

        helper.assertTrue(second.getMainHandItem().is(Items.BUCKET), "a small cow gave a second bucket today");
        assertFamiliarity(helper, cow, 0.75);
        helper.succeed();
    }

    public static void shearingCostsFamiliarity(GameTestHelper helper) {
        Sheep sheep = (Sheep) withFamiliarity(helper, female(helper, EntityTypes.SHEEP), 0.8);

        GameTestCompat.useOn(sheep, holding(helper, Items.SHEARS));

        helper.assertTrue(sheep.isSheared(), "a familiar sheep was not sheared");
        assertFamiliarity(helper, sheep, 0.75);
        helper.succeed();
    }

    // A dispenser shears through the sheep's own shear, the same as shears in hand.
    public static void dispenserShearingCostsFamiliarity(GameTestHelper helper) {
        Sheep sheep = (Sheep) withFamiliarity(helper, female(helper, EntityTypes.SHEEP), 0.8);

        //? if >=1.21.2 {
        sheep.shear(helper.getLevel(), SoundSource.BLOCKS, new ItemStack(Items.SHEARS));
        //?} else
        //sheep.shear(SoundSource.BLOCKS);

        assertFamiliarity(helper, sheep, 0.75);
        helper.succeed();
    }

    public static void mooshroomStewCostsFamiliarity(GameTestHelper helper) {
        Animal mooshroom = withFamiliarity(helper, female(helper, EntityTypes.MOOSHROOM), 0.8);
        Player player = holding(helper, Items.BOWL);

        GameTestCompat.interact(mooshroom, player);

        helper.assertTrue(player.getMainHandItem().is(Items.MUSHROOM_STEW), "the mooshroom gave " + player.getMainHandItem());
        assertFamiliarity(helper, mooshroom, 0.75);
        helper.succeed();
    }

    // Hungry on days 10 and 11, so on day 12 she has no milk, and the herd book agrees.
    public static void hungryCowGivesNoMilkNextDay(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        FamiliarityCare.settle(cow, clock(10L));
        long before = helper.getLevel().getOverworldClockTime();
        Player player = holding(helper, Items.BUCKET);
        HerdEntry entry;
        try {
            GameTestCompat.setOverworldClock(helper, clock(12L) + 1_000L);
            GameTestCompat.interact(cow, player);
            HerdBookData data = HerdBookData.get(helper.getLevel().getServer());
            entry = HerdBooks.entries(helper.getLevel().getServer(), data.ledger().books().keySet().stream()
                            .filter(book -> data.ledger().isMember(book, cow.getUUID())).findFirst().orElseThrow(), helper.getLevel().getGameTime())
                    .getFirst();
        } finally {
            GameTestCompat.setOverworldClock(helper, before);
        }

        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "a cow that went hungry yesterday gave " + player.getMainHandItem());
        helper.assertTrue(!entry.milkReady(), "the herd book shows milk for a cow that went hungry yesterday");
        helper.succeed();
    }

    // A day she can't be milked because she was hungry doesn't count towards drying up.
    public static void hungryDayDoesNotDryHerUp(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        long keep = BarnyardConfig.current().tuning().milkKeepsFlowingTicks();
        long endsBefore = traits(helper, cow).lactation().endsAt(keep);
        FamiliarityCare.settle(cow, clock(10L));

        FamiliarityCare.settle(cow, clock(12L));

        long endsAfter = traits(helper, cow).lactation().endsAt(keep);
        helper.assertTrue(endsAfter == endsBefore + 2L * GameDay.TICKS, "two hungry days moved her milk's end by " + (endsAfter - endsBefore));
        helper.succeed();
    }

    static long clock(long day) {
        return day * GameDay.TICKS;
    }

    static Animal withFamiliarity(GameTestHelper helper, Animal animal, double familiarity) {
        AnimalTraits traits = traits(helper, animal);
        Services.DATA.set(animal, traits.fed(familiarity, traits.lastFedTick()));
        return animal;
    }
}
