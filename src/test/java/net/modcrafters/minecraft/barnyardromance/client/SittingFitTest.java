package net.modcrafters.minecraft.barnyardromance.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SittingFitTest {
    private static final double PIXEL = 1.0 / 16.0;
    private static final double TURKEY_HITBOX = 0.8;
    // Naturalist's turkey model, in pixels at full size. Its tail fan is what makes it this tall and wide.
    private static final double TURKEY_TALL = 24.5;
    private static final double TURKEY_WIDE = 25.0;
    private static final double TURKEY_LEGS = 5.0;
    // The inside of the box.
    private static final double HOLLOW_TALL = 10.0;
    private static final double HOLLOW_WIDE = 14.0;

    @Test
    void chickenSitsFullSizeWithHerLegsInTheHay() {
        SittingFit fit = SittingFit.of("minecraft:chicken", 0.7, 1.0);
        assertEquals(1.0F, fit.scale());
        assertEquals(0.28, fit.sinkBy(), 1.0E-9);
    }

    @Test
    void aTallBirdIsSizedByItsHitbox() {
        SittingFit fit = SittingFit.of("naturalist:duck", 1.0, 1.0);
        assertEquals(0.9375F, fit.scale(), 1.0E-6F);
        assertEquals(0.4, fit.sinkBy(), 1.0E-9);
    }

    @Test
    void turkeyFitsWhateverHerSize() {
        for (double size : new double[]{0.7, 0.8, 0.9, 1.2}) {
            SittingFit fit = SittingFit.of("naturalist:turkey", TURKEY_HITBOX * size, size);
            double drawn = size * fit.scale();
            assertTrue((TURKEY_TALL - TURKEY_LEGS) * drawn <= HOLLOW_TALL, "her fan is through the roof at size " + size);
            assertTrue(TURKEY_WIDE * drawn <= HOLLOW_WIDE, "her fan is through the walls at size " + size);
            assertEquals(TURKEY_LEGS * PIXEL * size, fit.sinkBy(), 1.0E-9, "her legs aren't in the hay at size " + size);
        }
    }
}
