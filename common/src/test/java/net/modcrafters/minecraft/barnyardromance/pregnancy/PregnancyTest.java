package net.modcrafters.minecraft.barnyardromance.pregnancy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PregnancyTest {
    @Test
    void oldPregnanciesDefaultToAnAverageFather() {
        Pregnancy pregnancy = new Pregnancy(0L, 1L, "minecraft:cow", 1.0, 1, 0.0);

        assertEquals(0.5, pregnancy.fatherWeight(), 1.0e-9);
        assertEquals(0.5, pregnancy.withConceivedAt(9L).fatherWeight(), 1.0e-9);
    }

    @Test
    void settingConceivedAtClearsTheBoost() {
        Pregnancy pregnancy = new Pregnancy(0L, 10_000L, "minecraft:cow", 1.0, 1, 0.0)
                .withCare(PregnancyCare.NONE.boost(0L, 2.0, 1_000L).progress(10_000L, 0.1).improve(0.3))
                .withConceivedAt(50L);

        assertEquals(50L, pregnancy.conceivedAt());
        assertEquals(0L, pregnancy.care().extraTicks(5_000L));
        assertEquals(false, pregnancy.care().boosting(60L));
        assertEquals(0.3, pregnancy.care().nutritionBonus(), 1.0e-9);
    }

    @Test
    void newPregnanciesHaveNoCare() {
        assertEquals(PregnancyCare.NONE, new Pregnancy(0L, 1L, "minecraft:cow", 1.0, 1, 0.0).care());
    }
}
