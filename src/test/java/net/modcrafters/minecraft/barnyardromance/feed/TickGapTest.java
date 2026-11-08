package net.modcrafters.minecraft.barnyardromance.feed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TickGapTest {
    @Test
    void theFirstTickIsABreak() {
        TickGap gap = new TickGap();
        gap.tick(100L);
        assertFalse(gap.ranOn());
    }

    @Test
    void tickingOnIsNoBreak() {
        TickGap gap = new TickGap();
        gap.tick(100L);
        gap.ranOn();
        gap.tick(101L);
        assertTrue(gap.ranOn());
    }

    // Ticking while it waits for its animals, it asks nothing for a long while: that is no break.
    @Test
    void aLongWaitIsNoBreak() {
        TickGap gap = new TickGap();
        gap.tick(100L);
        gap.ranOn();
        for (long tick = 101L; tick <= 900L; tick++) {
            gap.tick(tick);
        }
        assertTrue(gap.ranOn());
    }

    // Unloaded during the wait: the break is kept until it is asked.
    @Test
    void aBreakDuringAWaitIsKept() {
        TickGap gap = new TickGap();
        gap.tick(100L);
        gap.ranOn();
        gap.tick(101L);
        gap.tick(500L);
        for (long tick = 501L; tick <= 900L; tick++) {
            gap.tick(tick);
        }
        assertFalse(gap.ranOn());
        gap.tick(901L);
        assertTrue(gap.ranOn());
    }
}
