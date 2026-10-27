package net.modcrafters.minecraft.barnyardromance.look;

import net.modcrafters.minecraft.barnyardromance.config.BellySettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BellyTest {
    private static final BellySettings BELLY = BellySettings.DEFAULTS;

    @Test
    void defaultsWidenFifteenPercentFromFortyPercentIn() {
        assertEquals(new BellySettings(0.15, 0.4), BellySettings.DEFAULTS);
    }

    @Test
    void nothingShowsUntilTheStart() {
        assertEquals(1.0, Belly.widen(0.0, BELLY), 1.0e-9);
        assertEquals(1.0, Belly.widen(0.4, BELLY), 1.0e-9);
    }

    @Test
    void fullWidthAtTheEnd() {
        assertEquals(1.15, Belly.widen(1.0, BELLY), 1.0e-9);
        assertEquals(1.15, Belly.widen(2.0, BELLY), 1.0e-9);
    }

    @Test
    void halfWayIsHalfWidth() {
        assertEquals(1.075, Belly.widen(0.7, BELLY), 1.0e-9);
    }

    @Test
    void widthOnlyGrows() {
        double last = Belly.widen(0.4, BELLY);
        for (int step = 41; step <= 100; step++) {
            double next = Belly.widen(step / 100.0, BELLY);
            assertTrue(next > last, "progress " + step);
            last = next;
        }
    }
}
