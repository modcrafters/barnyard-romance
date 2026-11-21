package net.modcrafters.minecraft.barnyardromance.breeding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TraderLlamasTest {
    @Test
    void traderStrengthIsThreeToFive() {
        assertEquals(3, TraderLlamas.strength(0.0));
        assertEquals(4, TraderLlamas.strength(0.34));
        assertEquals(5, TraderLlamas.strength(0.67));
        assertEquals(5, TraderLlamas.strength(0.999));
    }
}
