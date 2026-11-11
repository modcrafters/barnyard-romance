package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.vigour.Vigour;
import net.modcrafters.minecraft.barnyardromance.vigour.VigourActions;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.CareGameTests.clock;
import static net.modcrafters.minecraft.barnyardromance.vigour.VigourSlices.VIGOUR;
import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;

// In-game checks for a male's Vigour: how meals and midnights fill it, and what a hungry male pays.
// Days are passed as overworld clock times, so a test can live through several days in one tick.
public final class VigourGameTests {
    private static final long DAY = 10L;

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("vigour_refills_at_midnight_after_a_fed_day", 20, VigourGameTests::vigourRefillsAtMidnightAfterAFedDay),
            new BreedingGameTests.Case("vigour_carries_over_after_a_hungry_day", 20, VigourGameTests::vigourCarriesOverAfterAHungryDay),
            new BreedingGameTests.Case("a_meal_after_a_missed_day_fills_vigour", 20, VigourGameTests::aMealAfterAMissedDayFillsVigour),
            new BreedingGameTests.Case("hungry_payment_costs_health_not_life", 20, VigourGameTests::hungryPaymentCostsHealthNotLife)
    );

    private VigourGameTests() {
    }

    // He ate yesterday as well, so today's meal doesn't fill him; the next midnight does.
    public static void vigourRefillsAtMidnightAfterAFedDay(GameTestHelper helper) {
        Animal bull = withVigour(helper, male(helper, EntityTypes.COW), 0.1, DAY - 1L);
        FamiliarityCare.markEaten(bull, clock(DAY) + 1_000L);
        assertVigour(helper, bull, 0.1, clock(DAY) + 2_000L);

        assertVigour(helper, bull, 1.0, clock(DAY + 1L) + 10L);
        helper.succeed();
    }

    public static void vigourCarriesOverAfterAHungryDay(GameTestHelper helper) {
        Animal bull = withVigour(helper, male(helper, EntityTypes.COW), 0.4, DAY - 2L);

        assertVigour(helper, bull, 0.4, clock(DAY + 1L) + 10L);
        helper.succeed();
    }

    public static void aMealAfterAMissedDayFillsVigour(GameTestHelper helper) {
        Animal bull = withVigour(helper, male(helper, EntityTypes.COW), 0.1, DAY - 2L);
        FamiliarityCare.markEaten(bull, clock(DAY) + 1_000L);

        assertVigour(helper, bull, 1.0, clock(DAY) + 2_000L);
        helper.succeed();
    }

    public static void hungryPaymentCostsHealthNotLife(GameTestHelper helper) {
        Animal bull = withVigour(helper, male(helper, EntityTypes.COW), 1.0, DAY - 2L);
        bull.setHealth(3.0F);
        long now = clock(DAY) + 1_000L;
        helper.assertTrue(VigourActions.hungry(bull, now), "a bull last fed two days ago isn't hungry");
        helper.assertTrue(VigourActions.canPay(bull, 0.3, 2.0, now), "a hungry bull at 3 health can't mate for 2");

        VigourActions.pay(bull, 0.3, 2.0, now);

        helper.assertTrue(Math.abs(bull.getHealth() - 1.0F) < 1.0e-4F, "health after paying is " + bull.getHealth());
        assertVigour(helper, bull, 0.7, now);
        helper.assertTrue(!VigourActions.canPay(bull, 0.3, 2.0, now), "a hungry bull at 1 health can still mate for 2");
        helper.succeed();
    }

    // Settled on DAY with the given Vigour, last fed on lastAteDay.
    public static Animal withVigour(GameTestHelper helper, Animal animal, double vigour, long lastAteDay) {
        AnimalTraits current = traits(helper, animal);
        Services.DATA.set(animal, current.with(CARE, current.get(CARE).withHunger(new Hunger(DAY, lastAteDay, -1L))).with(VIGOUR, new Vigour(vigour, DAY, -1L)));
        return animal;
    }

    public static void assertVigour(GameTestHelper helper, Animal animal, double expected, long clock) {
        double actual = VigourActions.current(animal, clock).map(traits -> traits.get(VIGOUR).value()).orElse(-1.0);
        helper.assertTrue(Math.abs(actual - expected) < 1.0e-6, "Vigour is " + actual + ", expected " + expected);
    }
}
