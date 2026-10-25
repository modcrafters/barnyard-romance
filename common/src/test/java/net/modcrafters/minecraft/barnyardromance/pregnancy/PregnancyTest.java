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
}
