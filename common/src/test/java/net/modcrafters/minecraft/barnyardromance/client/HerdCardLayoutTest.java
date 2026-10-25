package net.modcrafters.minecraft.barnyardromance.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdCardLayoutTest {
    @Test
    void heartsFollowVanillaHalves() {
        assertEquals(5, HerdCardLayout.hearts(10.0F));
        assertEquals(1, HerdCardLayout.hearts(1.0F));
        assertEquals(2, HerdCardLayout.heartRows(11));
    }

    @Test
    void aLongNameOrMoreHeartsMakesTheCardWider() {
        int plain = HerdCardLayout.width(40, 57, 40);
        assertTrue(HerdCardLayout.width(80, 57, 40) > plain);
        assertTrue(HerdCardLayout.width(40, 57, 80) > plain);
    }

    @Test
    void extraHeartRowsMakeTheCardTaller() {
        assertTrue(HerdCardLayout.height(2) > HerdCardLayout.height(1));
        assertTrue(HerdCardLayout.height(1) >= HerdCardLayout.PAD * 2 + HerdCardLayout.PORTRAIT);
    }
}
