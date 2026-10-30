package net.modcrafters.minecraft.barnyardromance.eggs;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EggScheduleTest {
    private static final double[] ROLLS = {0.9, 0.0, 0.5};

    @Test
    void sinceMidnight() {
        assertEquals(0, EggSchedule.sinceMidnight(18_000L));
        assertEquals(6_000, EggSchedule.sinceMidnight(0L));
        assertEquals(18_000, EggSchedule.sinceMidnight(12_000L));
        assertEquals(0, EggSchedule.sinceMidnight(42_000L));
    }

    @Test
    void planFromMidnightIsDaytime() {
        List<Integer> times = EggSchedule.plan(0, 3, ROLLS);

        assertEquals(3, times.size());
        assertSortedWithin(times, 6_000, 18_000);
    }

    @Test
    void planAfterSunriseStartsNow() {
        List<Integer> times = EggSchedule.plan(10_000, 2, ROLLS);

        assertEquals(2, times.size());
        assertSortedWithin(times, 10_000, 18_000);
    }

    @Test
    void planAfterSunsetUsesRestOfDay() {
        List<Integer> times = EggSchedule.plan(20_000, 2, ROLLS);

        assertEquals(2, times.size());
        assertSortedWithin(times, 20_000, 24_000);
    }

    @Test
    void planOfNothingIsEmpty() {
        assertEquals(List.of(), EggSchedule.plan(0, 0, ROLLS));
    }

    @Test
    void dueLaysOnTime() {
        List<Integer> times = List.of(7_000, 9_000);

        assertEquals(new EggSchedule.Due(0, true), EggSchedule.due(times, 0, 7_000));
        assertEquals(new EggSchedule.Due(0, false), EggSchedule.due(times, 0, 6_999));
    }

    @Test
    void overdueEggsAreSkipped() {
        List<Integer> times = List.of(7_000, 9_000, 15_000);

        assertEquals(new EggSchedule.Due(1, true), EggSchedule.due(times, 0, 9_100));
        assertEquals(new EggSchedule.Due(2, false), EggSchedule.due(times, 0, 9_500));
    }

    @Test
    void nothingLeftIsNeverDue() {
        assertEquals(new EggSchedule.Due(0, false), EggSchedule.due(List.of(7_000), 1, 7_000));
    }

    private static void assertSortedWithin(List<Integer> times, int from, int until) {
        int previous = Integer.MIN_VALUE;
        for (int time : times) {
            assertTrue(time >= from && time < until, time + " outside [" + from + ", " + until + ")");
            assertTrue(time >= previous, "not sorted: " + times);
            previous = time;
        }
    }
}
