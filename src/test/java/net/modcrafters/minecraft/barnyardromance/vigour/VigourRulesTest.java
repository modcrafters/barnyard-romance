package net.modcrafters.minecraft.barnyardromance.vigour;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VigourRulesTest {
    private static final double EPSILON = 1.0e-9;

    @Test
    void settle_refillsAfterAFedDay() {
        assertEquals(new Vigour(1.0, 6L, -1L), VigourRules.settle(new Vigour(0.1, 5L, -1L), 5L, 6L));
    }

    @Test
    void settle_carriesOverAfterAHungryDay() {
        assertEquals(new Vigour(0.4, 7L, -1L), VigourRules.settle(new Vigour(0.4, 5L, -1L), 4L, 7L));
    }

    @Test
    void settle_refillsAfterAnyMealSinceLastSettle() {
        assertEquals(new Vigour(1.0, 9L, -1L), VigourRules.settle(new Vigour(0.0, 5L, -1L), 6L, 9L));
    }

    @Test
    void settle_sameDayKeepsValue() {
        assertEquals(new Vigour(0.4, 6L, -1L), VigourRules.settle(new Vigour(0.4, 6L, -1L), 6L, 6L));
    }

    @Test
    void settle_keepsTheRollDay() {
        assertEquals(new Vigour(1.0, 6L, 5L), VigourRules.settle(new Vigour(0.2, 5L, 5L), 5L, 6L));
    }

    @Test
    void settle_aNewMaleStartsFull() {
        assertEquals(new Vigour(1.0, 6L, -1L), VigourRules.settle(Vigour.NONE, -1L, 6L));
    }

    @Test
    void onMeal_fillsAfterMissingYesterday() {
        assertEquals(1.0, VigourRules.onMeal(new Vigour(0.1, 7L, -1L), 5L, 7L).value(), EPSILON);
    }

    @Test
    void onMeal_doesNotRefillAFedMale() {
        assertEquals(0.4, VigourRules.onMeal(new Vigour(0.4, 7L, -1L), 6L, 7L).value(), EPSILON);
    }

    @Test
    void onMeal_secondMealTodayDoesNothing() {
        assertEquals(0.7, VigourRules.onMeal(new Vigour(0.7, 7L, -1L), 7L, 7L).value(), EPSILON);
    }

    @Test
    void hungry_onlyWhenNeitherDay() {
        assertFalse(VigourRules.hungry(6L, 7L));
        assertFalse(VigourRules.hungry(7L, 7L));
        assertTrue(VigourRules.hungry(5L, 7L));
        assertTrue(VigourRules.hungry(-1L, 7L));
    }

    @Test
    void hungry_neverFedIsHungryOnTheFirstDay() {
        assertTrue(VigourRules.hungry(-1L, 0L));
    }

    @Test
    void onMeal_neverFedMaleFillsOnTheFirstDay() {
        assertEquals(1.0, VigourRules.onMeal(new Vigour(0.2, 0L, -1L), -1L, 0L).value(), EPSILON);
    }

    @Test
    void canPay_needsVigour() {
        assertTrue(VigourRules.canPay(0.3, 0.3, false, 10.0, 2.0));
        assertFalse(VigourRules.canPay(0.1, 0.3, false, 10.0, 2.0));
    }

    @Test
    void canPay_hungryNeedsHealthLeft() {
        assertTrue(VigourRules.canPay(1.0, 0.3, true, 3.0, 2.0));
    }

    @Test
    void canPay_refusesWhenHealthWouldReachZero() {
        assertFalse(VigourRules.canPay(1.0, 0.3, true, 2.0, 2.0));
    }

    @Test
    void canPay_aFedMaleIgnoresHisHealth() {
        assertTrue(VigourRules.canPay(1.0, 0.3, false, 1.0, 2.0));
    }

    @Test
    void pay_and_add_clamp() {
        assertEquals(0.0, VigourRules.pay(new Vigour(0.1, 3L, -1L), 0.3).value(), EPSILON);
        assertEquals(1.0, VigourRules.add(new Vigour(0.8, 3L, -1L), 0.5).value(), EPSILON);
    }

    @Test
    void threeMatingsLeaveTen() {
        Vigour vigour = new Vigour(1.0, 3L, -1L);
        for (int i = 0; i < 3; i++) {
            assertTrue(VigourRules.canPay(vigour.value(), 0.3, false, 10.0, 2.0), "mating " + (i + 1));
            vigour = VigourRules.pay(vigour, 0.3);
        }
        assertEquals(0.1, vigour.value(), 1.0e-6);
        assertFalse(VigourRules.canPay(vigour.value(), 0.3, false, 10.0, 2.0));
    }

    @Test
    void sixEggsLeaveTen() {
        Vigour vigour = new Vigour(1.0, 3L, -1L);
        for (int i = 0; i < 6; i++) {
            assertTrue(VigourRules.canPay(vigour.value(), 0.15, false, 10.0, 1.0), "egg " + (i + 1));
            vigour = VigourRules.pay(vigour, 0.15);
        }
        assertFalse(VigourRules.canPay(vigour.value(), 0.15, false, 10.0, 1.0));
    }
}
