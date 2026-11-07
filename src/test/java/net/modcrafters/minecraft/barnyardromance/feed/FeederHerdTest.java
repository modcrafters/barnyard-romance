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

    // At the usual reach of 16 a block touches three chunks along each axis, so nine in all, wherever it stands.
    @Test
    void theReachTouchesThreeChunksEachWay() {
        assertEquals(-1, FeederHerd.firstChunk(0, 16));
        assertEquals(1, FeederHerd.lastChunk(0, 16));
        assertEquals(-1, FeederHerd.firstChunk(15, 16));
        assertEquals(1, FeederHerd.lastChunk(15, 16));
        assertEquals(0, FeederHerd.firstChunk(16, 16));
        assertEquals(2, FeederHerd.lastChunk(16, 16));
        assertEquals(-3, FeederHerd.firstChunk(-17, 16));
        assertEquals(-1, FeederHerd.lastChunk(-17, 16));
        for (int block = -40; block <= 40; block++) {
            assertEquals(3, FeederHerd.lastChunk(block, 16) - FeederHerd.firstChunk(block, 16) + 1, "block " + block);
        }
        assertEquals(0, FeederHerd.firstChunk(5, 0));
        assertEquals(0, FeederHerd.lastChunk(5, 0));
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
