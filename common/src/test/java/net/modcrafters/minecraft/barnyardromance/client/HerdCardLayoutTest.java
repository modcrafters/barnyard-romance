package net.modcrafters.minecraft.barnyardromance.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdCardLayoutTest {
    @Test
    void aPregnancyRowMakesTheCardTallerAndWider() {
        int plainWidth = HerdCardLayout.width(40, 80, 0, false);
        int pregnantWidth = HerdCardLayout.width(40, 80, 120, true);

        assertTrue(pregnantWidth > plainWidth);
        assertTrue(HerdCardLayout.height(true) > HerdCardLayout.height(false));
    }

    @Test
    void thePortraitFitsInsideTheCard() {
        assertTrue(HerdCardLayout.height(false) >= HerdCardLayout.PAD * 2 + HerdCardLayout.PORTRAIT);
    }
}
