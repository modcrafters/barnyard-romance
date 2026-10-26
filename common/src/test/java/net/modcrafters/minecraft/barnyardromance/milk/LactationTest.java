package net.modcrafters.minecraft.barnyardromance.milk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LactationTest {
    private static final long KEEP = 72_000L;

    @Test
    void noneHasNoMilk() {
        assertFalse(Lactation.NONE.hasMilk(0L, KEEP));
        assertFalse(Lactation.NONE.hasMilk(1_000_000L, KEEP));
    }

    @Test
    void birthStartsMilk() {
        Lactation lactation = Lactation.NONE.afterBirth(1_000L, 25_000L, KEEP);

        assertTrue(lactation.hasMilk(1_000L, KEEP));
        assertTrue(lactation.graceGiven());
    }

    @Test
    void milkFlowsWhileTheBabyIsYoung() {
        Lactation lactation = new Lactation(500L, 200_000L, -1L, 0, true);

        assertTrue(lactation.nursing(150_000L));
        assertTrue(lactation.hasMilk(150_000L, KEEP));
    }

    @Test
    void milkEndsThreeDaysAfterTheBabyGrows() {
        Lactation lactation = Lactation.NONE.afterBirth(0L, 24_000L, 1L);

        assertTrue(lactation.hasMilk(24_000L + KEEP - 1, KEEP));
        assertFalse(lactation.hasMilk(24_000L + KEEP, KEEP));
    }

    @Test
    void milkingKeepsItFlowing() {
        Lactation lactation = Lactation.NONE.afterBirth(0L, 24_000L, 1L).milked(90_000L, KEEP);

        assertTrue(lactation.hasMilk(90_000L + KEEP - 1, KEEP));
        assertFalse(lactation.hasMilk(90_000L + KEEP, KEEP));
    }

    @Test
    void largeFemalesMilkTwice() {
        Lactation lactation = Lactation.NONE.afterBirth(0L, 24_000L, KEEP);

        assertEquals(2, lactation.milkingsLeft(100L, 2));
        lactation = lactation.milked(100L, KEEP);
        assertEquals(1, lactation.milkingsLeft(200L, 2));
        lactation = lactation.milked(200L, KEEP);
        assertEquals(0, lactation.milkingsLeft(300L, 2));
    }

    @Test
    void windowResetsAfterADay() {
        Lactation lactation = Lactation.NONE.afterBirth(0L, 24_000L, KEEP).milked(100L, KEEP).milked(200L, KEEP);

        assertEquals(0, lactation.milkingsLeft(100L + 23_999L, 2));
        assertEquals(2, lactation.milkingsLeft(100L + 24_000L, 2));
        Lactation next = lactation.milked(100L + 24_000L, KEEP);
        assertEquals(1, next.milkingsLeft(100L + 24_001L, 2));
    }

    @Test
    void graceIsGivenOnce() {
        Lactation first = Lactation.NONE.grace(1_000L, KEEP);
        Lactation second = first.grace(50_000L, KEEP);

        assertTrue(first.hasMilk(1_000L, KEEP));
        assertEquals(1_000L + KEEP, second.milkUntil());
        assertTrue(second.graceGiven());
    }
}
