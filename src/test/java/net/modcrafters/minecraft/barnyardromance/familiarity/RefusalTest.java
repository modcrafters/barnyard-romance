package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.familiarity.api.Refusable;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.config.Tuning;
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
        Refusals refusals = Refusals.NONE.with(Refusable.MILKING, 12L);

        assertTrue(refusals.refused(Refusable.MILKING, 12L));
        assertFalse(refusals.refused(Refusable.MILKING, 13L));
        assertFalse(refusals.refused(Refusable.SHEARING, 12L));
        assertEquals(12L, refusals.with(Refusable.SHEARING, 14L).milkDay());
        assertTrue(refusals.with(Refusable.SHEARING, 14L).refused(Refusable.SHEARING, 14L));
    }
}
