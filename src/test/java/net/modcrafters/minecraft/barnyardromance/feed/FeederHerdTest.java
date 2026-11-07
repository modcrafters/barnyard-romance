package net.modcrafters.minecraft.barnyardromance.feed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeederHerdTest {
    @Test
    void refreshSlotIsInRange() {
        int[] hashes = {0, 1, -1, 1199, 1200, -1200, 123_456_789, -987_654_321, Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (int hash : hashes) {
            int slot = FeederHerd.refreshSlot(hash);
            assertTrue(slot >= 0 && slot < FeederHerd.REFRESH_TICKS, "slot " + slot + " for hash " + hash);
            assertEquals(Math.floorMod(hash, 1200), slot);
        }
    }

    @Test
    void aRefreshIsDueOnceAMinute() {
        int slot = FeederHerd.refreshSlot(-7);
        int due = 0;
        for (long tick = 5_000L; tick < 5_000L + FeederHerd.REFRESH_TICKS; tick++) {
            if (FeederHerd.due(tick, slot)) {
                due++;
                assertEquals(slot, (int) (tick % FeederHerd.REFRESH_TICKS));
            }
        }
        assertEquals(1, due);
    }
}
