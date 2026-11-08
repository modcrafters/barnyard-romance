package net.modcrafters.minecraft.barnyardromance.feed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeederFillTest {
    @Test
    void representsTwoDaysForTheWholeHerd() {
        assertEquals(0, FeederFill.level(0, 4, 2));
        assertEquals(1, FeederFill.level(1, 4, 2));
        assertEquals(1, FeederFill.level(2, 4, 2));
        assertEquals(2, FeederFill.level(4, 4, 2));
        assertEquals(3, FeederFill.level(6, 4, 2));
        assertEquals(3, FeederFill.level(7, 4, 2));
        assertEquals(4, FeederFill.level(8, 4, 2));
        assertEquals(4, FeederFill.level(576, 4, 2));
    }

    @Test
    void changingHerdOrTargetDaysChangesTheDisplay() {
        assertEquals(4, FeederFill.level(8, 4, 2));
        assertEquals(2, FeederFill.level(8, 8, 2));
        assertEquals(2, FeederFill.level(8, 4, 4));
        assertEquals(1, FeederFill.level(1, 200, 365));
    }

    @Test
    void anUnknownHerdUsesOneAnimalLikeTheWindow() {
        assertEquals(0, FeederFill.level(0, 0, 2));
        assertEquals(2, FeederFill.level(1, 0, 2));
        assertEquals(4, FeederFill.level(2, 0, 2));
    }

    @Test
    void largeCountsCannotOverflow() {
        assertEquals(4, FeederFill.level(Integer.MAX_VALUE, 1, 2));
        assertEquals(1, FeederFill.level(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
}
