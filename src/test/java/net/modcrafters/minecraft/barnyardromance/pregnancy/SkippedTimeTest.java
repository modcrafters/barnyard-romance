package net.modcrafters.minecraft.barnyardromance.pregnancy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkippedTimeTest {
    @Test
    void timePassingNormallyDriftsNothing() {
        assertEquals(0L, new SkippedTime(1_000L, 7_000L).drift(1_500L, 7_500L));
    }

    @Test
    void aClockMovedOnDriftsAhead() {
        assertEquals(24_000L, new SkippedTime(1_000L, 7_000L).drift(1_020L, 31_020L));
    }

    @Test
    void aStoppedOrRewoundClockDriftsBehind() {
        assertEquals(-500L, new SkippedTime(1_000L, 7_000L).drift(1_500L, 7_000L));
        assertEquals(-6_020L, new SkippedTime(1_000L, 7_000L).drift(1_020L, 1_000L));
    }

    @Test
    void anUnseenPregnancyDriftsNothing() {
        assertFalse(SkippedTime.UNSEEN.seen());
        assertTrue(new SkippedTime(0L, 0L).seen());
        assertEquals(0L, SkippedTime.UNSEEN.drift(1_020L, 31_020L));
    }

    @Test
    void skippingIsTheSameAsThatMuchTimePassing() {
        Pregnancy pregnancy = new Pregnancy(1_000L, 24_000L, "minecraft:cow", 1.0, 1, 0.0)
                .withCare(PregnancyCare.NONE.boost(5_000L, 2.0, 4_000L));
        Pregnancy skipped = pregnancy.skipped(3_000L, new SkippedTime(6_000L, 9_000L));
        for (long now : new long[] {5_000L, 6_000L, 7_500L, 10_000L, 20_000L}) {
            assertEquals(PregnancyClock.progress(now + 3_000L, pregnancy), PregnancyClock.progress(now, skipped), 1.0e-9, "at " + now);
            assertEquals(PregnancyClock.remainingTicks(now + 3_000L, pregnancy), PregnancyClock.remainingTicks(now, skipped), "at " + now);
        }
        assertEquals(new SkippedTime(6_000L, 9_000L), skipped.seen());
    }
}
