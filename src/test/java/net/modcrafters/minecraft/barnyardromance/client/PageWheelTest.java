package net.modcrafters.minecraft.barnyardromance.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageWheelTest {
    @Test
    void aWheelNotchTurnsOnePage() {
        PageWheel wheel = new PageWheel();

        assertEquals(-1, wheel.scroll(-1.0));
        assertEquals(1, wheel.scroll(1.0));
    }

    @Test
    void smallScrollsGatherIntoOneTurn() {
        PageWheel wheel = new PageWheel();

        assertEquals(0, wheel.scroll(0.25));
        assertEquals(0, wheel.scroll(0.25));
        assertEquals(0, wheel.scroll(0.25));
        assertEquals(1, wheel.scroll(0.25));
        assertEquals(0, wheel.scroll(0.25));
    }

    @Test
    void whatIsLeftOverCountsTowardsTheNextTurn() {
        PageWheel wheel = new PageWheel();

        assertEquals(-1, wheel.scroll(-1.5));
        assertEquals(-1, wheel.scroll(-0.5));
    }

    @Test
    void changingDirectionStartsOver() {
        PageWheel wheel = new PageWheel();

        assertEquals(0, wheel.scroll(0.75));
        assertEquals(0, wheel.scroll(-0.5));
        assertEquals(-1, wheel.scroll(-0.5));
    }

    @Test
    void noVerticalScrollKeepsWhatWasGathered() {
        PageWheel wheel = new PageWheel();

        assertEquals(0, wheel.scroll(0.75));
        assertEquals(0, wheel.scroll(0.0));
        assertEquals(1, wheel.scroll(0.25));
    }
}
