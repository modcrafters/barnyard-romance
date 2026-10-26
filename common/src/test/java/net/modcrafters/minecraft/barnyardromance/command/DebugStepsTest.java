package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyCare;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyClock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DebugStepsTest {
    @Test
    void familiarityMovesByTenPercentAndStopsAtTheEnds() {
        assertEquals(0.10, DebugSteps.familiarity(0.0, 1), 1.0e-9);
        assertEquals(0.45, DebugSteps.familiarity(0.35, 1), 1.0e-9);
        assertEquals(1.0, DebugSteps.familiarity(0.95, 1), 1.0e-9);
        assertEquals(0.0, DebugSteps.familiarity(0.05, -1), 1.0e-9);
    }

    @Test
    void familiarityCanBeSetToAnExactPercent() {
        assertEquals(0.0, DebugSteps.familiarityAt(0), 1.0e-9);
        assertEquals(0.35, DebugSteps.familiarityAt(35), 1.0e-9);
        assertEquals(1.0, DebugSteps.familiarityAt(100), 1.0e-9);
    }

    @Test
    void pregnancyMovesByTenPercentOfItsDuration() {
        Pregnancy pregnancy = new Pregnancy(1_000L, 10_000L, "minecraft:cow", 1.1, 1, 0.4);

        Pregnancy forward = DebugSteps.pregnancy(pregnancy, 1_000L, 1);
        Pregnancy back = DebugSteps.pregnancy(forward, 1_000L, -1);

        assertEquals(0L, forward.conceivedAt());
        assertEquals(pregnancy.conceivedAt(), back.conceivedAt());
        assertEquals(pregnancy.motherFamiliarity(), forward.motherFamiliarity(), 1.0e-9);
    }

    @Test
    void pregnancyStopsAtConceptionAndAtBirth() {
        Pregnancy fresh = new Pregnancy(5_000L, 10_000L, "minecraft:pig", 1.0, 2, 0.2);
        Pregnancy due = new Pregnancy(0L, 10_000L, "minecraft:pig", 1.0, 2, 0.2);

        assertEquals(5_000L, DebugSteps.pregnancy(fresh, 5_000L, -1).conceivedAt());
        assertEquals(0L, DebugSteps.pregnancy(due, 10_000L, 1).conceivedAt());
    }

    @Test
    void pregnancyCanBeSetToAnExactPercent() {
        Pregnancy pregnancy = new Pregnancy(1_000L, 10_000L, "minecraft:cow", 1.1, 1, 0.4);

        assertEquals(8_000L, DebugSteps.pregnancyAt(pregnancy, 8_000L, 0).conceivedAt());
        assertEquals(5_500L, DebugSteps.pregnancyAt(pregnancy, 8_000L, 25).conceivedAt());
        assertEquals(-2_000L, DebugSteps.pregnancyAt(pregnancy, 8_000L, 100).conceivedAt());
        assertEquals(pregnancy.litterSize(), DebugSteps.pregnancyAt(pregnancy, 8_000L, 25).litterSize());
    }

    @Test
    void pregnancyStepsFromTheProgressShown() {
        Pregnancy fed = new Pregnancy(0L, 10_000L, "minecraft:cow", 1.0, 1, 0.0)
                .withCare(PregnancyCare.NONE.progress(10_000L, 0.3));

        Pregnancy next = DebugSteps.pregnancy(fed, 2_000L, 1);

        assertEquals(0.6, PregnancyClock.progress(2_000L, next), 1.0e-9);
    }
}
