package net.modcrafters.minecraft.barnyardromance.familiarity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductGateTest {
    @Test
    void milkAndWoolWaitForTheSameThresholdAsBreeding() {
        assertTrue(ProductGate.blocked(true, true, 0.2));
        assertFalse(ProductGate.blocked(true, true, 0.3));
    }

    @Test
    void aDisabledOrUntrackedAnimalKeepsVanillaProducts() {
        assertFalse(ProductGate.blocked(false, true, 0.0));
        assertFalse(ProductGate.blocked(true, false, 0.0));
    }
}
