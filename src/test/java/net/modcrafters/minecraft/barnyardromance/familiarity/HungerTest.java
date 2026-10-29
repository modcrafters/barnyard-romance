package net.modcrafters.minecraft.barnyardromance.familiarity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HungerTest {
    @Test
    void firstSettleCountsNothing() {
        Hunger.Settled settled = Hunger.settle(Hunger.NONE, 10L, false);

        assertEquals(0, settled.hungryDays());
        assertTrue(settled.first());
        assertEquals(10L, settled.hunger().settledDay());
    }

    @Test
    void fedEveryDayNeverLoses() {
        Hunger hunger = Hunger.settle(Hunger.NONE, 10L, false).hunger();
        int hungry = 0;
        for (long day = 10L; day < 20L; day++) {
            hunger = hunger.ate(day);
            Hunger.Settled settled = Hunger.settle(hunger, day + 1L, false);
            hungry += settled.hungryDays();
            hunger = settled.hunger();
        }

        assertEquals(0, hungry);
    }

    @Test
    void threeHungryDays() {
        Hunger hunger = new Hunger(10L, 10L, -1L);

        Hunger.Settled settled = Hunger.settle(hunger, 14L, false);

        assertEquals(3, settled.hungryDays());
        assertFalse(settled.first());
        assertEquals(13L, settled.hunger().lastHungryDay());
        assertEquals(14L, settled.hunger().settledDay());
    }

    @Test
    void theSettledDayItselfCountsWhenNothingWasEaten() {
        Hunger.Settled settled = Hunger.settle(new Hunger(10L, -1L, -1L), 12L, false);

        assertEquals(2, settled.hungryDays());
    }

    @Test
    void settleTwiceSameDayIsNoOp() {
        Hunger.Settled once = Hunger.settle(new Hunger(10L, 10L, -1L), 14L, false);

        Hunger.Settled twice = Hunger.settle(once.hunger(), 14L, false);

        assertEquals(0, twice.hungryDays());
        assertEquals(once.hunger(), twice.hunger());
    }

    @Test
    void nursingIsNeverHungry() {
        Hunger.Settled settled = Hunger.settle(new Hunger(10L, 10L, -1L), 14L, true);

        assertEquals(0, settled.hungryDays());
        assertEquals(14L, settled.hunger().settledDay());
        assertEquals(-1L, settled.hunger().lastHungryDay());
    }

    @Test
    void hungryYesterday() {
        Hunger hunger = new Hunger(14L, 10L, 13L);

        assertTrue(hunger.hungryYesterday(14L));
        assertFalse(hunger.hungryYesterday(15L));
    }

    @Test
    void afterHungerClamps() {
        assertEquals(0.0, Hunger.afterHunger(0.08, 3, 0.05), 1.0e-9);
        assertEquals(0.3, Hunger.afterHunger(1.0, 14, 0.05), 1.0e-9);
        assertEquals(0.8, Hunger.afterHunger(0.8, 0, 0.05), 1.0e-9);
    }
}
