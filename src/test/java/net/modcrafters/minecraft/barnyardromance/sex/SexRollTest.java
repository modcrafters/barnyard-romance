package net.modcrafters.minecraft.barnyardromance.sex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SexRollTest {
    @Test
    void valuesBelowTheFemaleRatioAreFemale() {
        assertEquals(Sex.FEMALE, SexRoll.roll(0.0, 0.5));
        assertEquals(Sex.FEMALE, SexRoll.roll(0.4999, 0.5));
    }

    @Test
    void valuesAtOrAboveTheFemaleRatioAreMale() {
        assertEquals(Sex.MALE, SexRoll.roll(0.5, 0.5));
        assertEquals(Sex.MALE, SexRoll.roll(0.999, 0.5));
    }

    @Test
    void aZeroRatioProducesOnlyMales() {
        assertEquals(Sex.MALE, SexRoll.roll(0.0, 0.0));
    }

    @Test
    void aOneRatioProducesOnlyFemales() {
        assertEquals(Sex.FEMALE, SexRoll.roll(0.999, 1.0));
    }
}
