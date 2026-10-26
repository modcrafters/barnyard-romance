package net.modcrafters.minecraft.barnyardromance.drops;

import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YieldTest {
    private static final Tuning TUNING = Tuning.DEFAULTS;

    @Test
    void malesYieldAQuarterMore() {
        assertEquals(1.25, Yield.sexAge(Sex.MALE, false, TUNING), 1.0e-9);
        assertEquals(1.0, Yield.sexAge(Sex.FEMALE, false, TUNING), 1.0e-9);
    }

    @Test
    void oldAnimalsYieldLess() {
        assertEquals(0.75, Yield.sexAge(Sex.FEMALE, true, TUNING), 1.0e-9);
        assertEquals(0.9375, Yield.sexAge(Sex.MALE, true, TUNING), 1.0e-9);
    }

    @Test
    void vanillaSitsAtTheBase() {
        assertEquals(1.0, Yield.size(1.1, 1.1, 1.0), 1.0e-9);
    }

    @Test
    void doubleScaleDoublesAtExponentOne() {
        assertEquals(2.0, Yield.size(2.0, 1.0, 1.0), 1.0e-9);
        assertEquals(0.5, Yield.size(0.5, 1.0, 1.0), 1.0e-9);
    }

    @Test
    void exponentZeroIgnoresSize() {
        assertEquals(1.0, Yield.size(3.0, 1.0, 0.0), 1.0e-9);
    }

    @Test
    void roundingKeepsTheAverage() {
        assertEquals(3, Yield.round(2.25, 0.2));
        assertEquals(2, Yield.round(2.25, 0.3));
    }

    @Test
    void wholeAmountsNeverRound() {
        assertEquals(4, Yield.round(4.0, 0.0));
        assertEquals(4, Yield.round(4.0, 0.999));
        assertEquals(0, Yield.round(-1.0, 0.0));
    }
}
