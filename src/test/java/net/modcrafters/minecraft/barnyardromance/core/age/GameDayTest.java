package net.modcrafters.minecraft.barnyardromance.core.age;

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

    @Test
    void ticksSinceMidnight() {
        assertEquals(0, GameDay.ticksSinceMidnight(18_000L));
        assertEquals(6_000, GameDay.ticksSinceMidnight(0L));
        assertEquals(18_000, GameDay.ticksSinceMidnight(12_000L));
        assertEquals(0, GameDay.ticksSinceMidnight(42_000L));
    }

    // Day 1 runs from midnight at 18,000 to the next at 42,000, with sunrise at 24,000.
    @Test
    void afterSunriseRunsFromSunriseToMidnight() {
        assertEquals(false, GameDay.afterSunrise(18_000L));
        assertEquals(false, GameDay.afterSunrise(23_999L));
        assertEquals(true, GameDay.afterSunrise(24_000L));
        assertEquals(true, GameDay.afterSunrise(41_999L));
        assertEquals(false, GameDay.afterSunrise(-1L));
        assertEquals(true, GameDay.afterSunrise(0L));
    }
}
