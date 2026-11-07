package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.familiarity.HungerDamage;
import net.modcrafters.minecraft.barnyardromance.familiarity.Origin;
import net.modcrafters.minecraft.barnyardromance.familiarity.Upkeep;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

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
            new BreedingGameTests.Case("an_old_save_herd_book_cow_counts_as_cared_for", 20, UpkeepGameTests::anOldSaveHerdBookCowCountsAsCaredFor)
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

    // A herd book animal from before upkeep was kept is cared for, however far it has fallen since.
    public static void anOldSaveHerdBookCowCountsAsCaredFor(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        HerdBooks.register(new ItemStack(BarnyardItems.HERD_BOOK), cow);
        cow(cow, traits(helper, cow), 0.1, 10.0F, DAY - 1L);

        FamiliarityCare.settle(cow, clock(DAY));

        helper.assertTrue(traits(helper, cow).upkeep().cared(), "an old save's herd book cow isn't cared for");
        helper.succeed();
    }

    // A farm-raised cow at the given familiarity and health, last settled on settledDay and unfed since.
    private static Animal cow(GameTestHelper helper, double familiarity, float health, long settledDay) {
        Animal cow = female(helper, EntityTypes.COW);
        return cow(cow, traits(helper, cow), familiarity, health, settledDay);
    }

    private static Animal cow(Animal cow, AnimalTraits traits, double familiarity, float health, long settledDay) {
        Services.DATA.set(cow, traits.fed(familiarity, -1L).withOrigin(Origin.FARM)
                .withHunger(new Hunger(settledDay, -1L, -1L)).withUpkeep(Upkeep.NONE));
        cow.setHealth(health);
        return cow;
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
