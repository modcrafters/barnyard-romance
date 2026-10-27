package net.modcrafters.minecraft.barnyardromance.look;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HornSidesTest {
    @Test
    void anySidedPartsAreAlwaysDrawn() {
        assertTrue(HornSides.drawn(HornSide.ANY, false, false));
    }

    @Test
    void aSidedPartNeedsItsHorn() {
        assertTrue(HornSides.drawn(HornSide.LEFT, true, false));
        assertFalse(HornSides.drawn(HornSide.LEFT, false, true));
        assertTrue(HornSides.drawn(HornSide.RIGHT, false, true));
        assertFalse(HornSides.drawn(HornSide.RIGHT, true, false));
    }
}
