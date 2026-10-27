package net.modcrafters.minecraft.barnyardromance.age;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameDayTest {
    // The overworld clock wakes sleepers at 0 and reaches midnight at 18,000, so days turn over there.
    @Test
    void aDayTurnsOverAtMidnight() {
        assertEquals(0L, GameDay.of(0L));
        assertEquals(0L, GameDay.of(17_999L));
        assertEquals(1L, GameDay.of(18_000L));
        assertEquals(1L, GameDay.of(41_999L));
        assertEquals(2L, GameDay.of(42_000L));
    }

    @Test
    void ticksUntilMidnightCountDownToTheNextOne() {
        assertEquals(18_000L, GameDay.ticksUntilMidnight(0L));
        assertEquals(1L, GameDay.ticksUntilMidnight(17_999L));
        assertEquals(24_000L, GameDay.ticksUntilMidnight(18_000L));
    }
}
