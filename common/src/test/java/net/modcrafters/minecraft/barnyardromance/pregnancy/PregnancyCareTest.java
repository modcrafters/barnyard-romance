package net.modcrafters.minecraft.barnyardromance.pregnancy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PregnancyCareTest {
    @Test
    void boostDoublesProgressWhileItRuns() {
        PregnancyCare care = PregnancyCare.NONE.boost(1_000L, 2.0, 24_000L);

        assertTrue(care.boosting(7_000L));
        assertEquals(6_000L, care.extraTicks(7_000L));
        assertEquals(0L, care.extraTicks(1_000L));
    }

    @Test
    void boostStopsAtItsEnd() {
        PregnancyCare care = PregnancyCare.NONE.boost(1_000L, 2.0, 24_000L);

        assertFalse(care.boosting(25_000L));
        assertEquals(24_000L, care.extraTicks(31_000L));
    }

    @Test
    void secondBoostBanksTheFirst() {
        PregnancyCare care = PregnancyCare.NONE.boost(0L, 2.0, 100L).boost(500L, 3.0, 100L);

        assertEquals(100L, care.bankedTicks());
        assertEquals(200L, care.extraTicks(550L));
        assertEquals(300L, care.extraTicks(1_000L));
    }

    @Test
    void negativeProgressSlowsButNeverGoesBelowZero() {
        Pregnancy pregnancy = new Pregnancy(0L, 10_000L, "minecraft:cow", 1.0, 1, 0.0)
                .withCare(PregnancyCare.NONE.progress(10_000L, -0.5));

        assertEquals(-5_000L, pregnancy.care().bankedTicks());
        assertEquals(0.0, PregnancyClock.progress(2_000L, pregnancy), 1.0e-9);
        assertEquals(0.5, PregnancyClock.progress(10_000L, pregnancy), 1.0e-9);
        assertFalse(PregnancyClock.ready(10_000L, pregnancy));
        assertTrue(PregnancyClock.ready(15_000L, pregnancy));
    }

    @Test
    void positiveProgressBringsTheBirthForward() {
        Pregnancy pregnancy = new Pregnancy(0L, 10_000L, "minecraft:cow", 1.0, 1, 0.0)
                .withCare(PregnancyCare.NONE.progress(10_000L, 0.25));

        assertEquals(0.75, PregnancyClock.progress(5_000L, pregnancy), 1.0e-9);
        assertTrue(PregnancyClock.ready(7_500L, pregnancy));
    }

    @Test
    void improveAddsToNutrition() {
        assertEquals(0.5, PregnancyCare.NONE.improve(0.25).improve(0.25).nutritionBonus(), 1.0e-9);
        assertEquals(1.0, PregnancyCare.NONE.improve(0.75).improve(0.75).nutritionBonus(), 1.0e-9);
    }
}
