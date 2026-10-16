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
}
