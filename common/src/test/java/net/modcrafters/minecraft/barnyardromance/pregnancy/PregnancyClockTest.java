package net.modcrafters.minecraft.barnyardromance.pregnancy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PregnancyClockTest {
    @Test
    void aNewPregnancyHasNotStarted() {
        assertEquals(0.0, PregnancyClock.progress(1_000, 1_000, 24_000), 1.0e-9);
        assertEquals(24_000, PregnancyClock.remainingTicks(1_000, 1_000, 24_000));
        assertFalse(PregnancyClock.ready(1_000, 1_000, 24_000));
    }

    @Test
    void halfwayThroughReportsHalfProgress() {
        assertEquals(0.5, PregnancyClock.progress(13_000, 1_000, 24_000), 1.0e-9);
        assertEquals(12_000, PregnancyClock.remainingTicks(13_000, 1_000, 24_000));
        assertFalse(PregnancyClock.ready(13_000, 1_000, 24_000));
    }

    @Test
    void timeSpentUnloadedStillCounts() {
        assertEquals(1.0, PregnancyClock.progress(50_000, 1_000, 24_000), 1.0e-9);
        assertEquals(0, PregnancyClock.remainingTicks(50_000, 1_000, 24_000));
        assertTrue(PregnancyClock.ready(50_000, 1_000, 24_000));
    }

    @Test
    void aClockThatHasNotReachedConceptionStaysAtTheStart() {
        assertEquals(0.0, PregnancyClock.progress(500, 1_000, 24_000), 1.0e-9);
        assertFalse(PregnancyClock.ready(500, 1_000, 24_000));
    }

    @Test
    void noCareMatchesTheOldClock() {
        Pregnancy pregnancy = new Pregnancy(1_000L, 24_000L, "minecraft:cow", 1.0, 1, 0.0);
        for (long now : new long[] {0L, 1_000L, 7_000L, 25_000L, 40_000L}) {
            assertEquals(PregnancyClock.progress(now, 1_000L, 24_000L), PregnancyClock.progress(now, pregnancy), 1.0e-12);
            assertEquals(PregnancyClock.remainingTicks(now, 1_000L, 24_000L), PregnancyClock.remainingTicks(now, pregnancy));
            assertEquals(PregnancyClock.ready(now, 1_000L, 24_000L), PregnancyClock.ready(now, pregnancy));
        }
    }

    @Test
    void remainingCountsTheBoost() {
        Pregnancy pregnancy = new Pregnancy(0L, 30_000L, "minecraft:cow", 1.0, 1, 0.0)
                .withCare(PregnancyCare.NONE.boost(24_000L, 2.0, 24_000L));

        assertEquals(3_000L, PregnancyClock.remainingTicks(24_000L, pregnancy));
        assertEquals(0.9, PregnancyClock.progress(25_500L, pregnancy), 1.0e-9);
    }

    @Test
    void remainingAfterAShortBoost() {
        Pregnancy pregnancy = new Pregnancy(0L, 100_000L, "minecraft:cow", 1.0, 1, 0.0)
                .withCare(PregnancyCare.NONE.boost(0L, 2.0, 1_000L));

        assertEquals(99_000L, PregnancyClock.remainingTicks(0L, pregnancy));
        assertEquals(97_000L, PregnancyClock.remainingTicks(2_000L, pregnancy));
    }
}
