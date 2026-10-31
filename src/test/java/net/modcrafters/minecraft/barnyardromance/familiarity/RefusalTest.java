package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefusalTest {
    @Test
    void refusesBelowTheLineOnALowRoll() {
        assertTrue(Refusal.refuses(0.4, 0.1, Tuning.DEFAULTS));
        assertFalse(Refusal.refuses(0.4, 0.2, Tuning.DEFAULTS));
    }

    @Test
    void neverAtOrAboveTheLine() {
        assertFalse(Refusal.refuses(0.5, 0.0, Tuning.DEFAULTS));
        // Familiarity moves in 5% steps that doubles can't hold exactly, so a hair under still counts as 50%.
        assertFalse(Refusal.refuses(0.5 - 1.0e-12, 0.0, Tuning.DEFAULTS));
    }

    @Test
    void chanceZeroTurnsRefusalsOff() {
        Tuning off = BarnyardConfig.parse("""
                { "tuning": { "refusalChance": 0 } }
                """).tuning();

        assertFalse(Refusal.refuses(0.0, 0.0, off));
    }

    @Test
    void refusalDaysAreKeptApart() {
        Refusals refusals = Refusals.NONE.withMilk(12L);

        assertTrue(refusals.milkRefused(12L));
        assertFalse(refusals.milkRefused(13L));
        assertFalse(refusals.shearRefused(12L));
        assertEquals(12L, refusals.withShear(14L).milkDay());
        assertTrue(refusals.withShear(14L).shearRefused(14L));
    }
}
