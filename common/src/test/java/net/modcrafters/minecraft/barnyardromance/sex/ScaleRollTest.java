package net.modcrafters.minecraft.barnyardromance.sex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScaleRollTest {
    private final SizeSettings defaults = new SizeSettings(0.9, 1.1, 0.05);

    @Test
    void unitRandomZeroIsBaseMinusVariation() {
        assertEquals(0.85, ScaleRoll.roll(Sex.FEMALE, defaults, 0.0), 1.0e-9);
        assertEquals(1.05, ScaleRoll.roll(Sex.MALE, defaults, 0.0), 1.0e-9);
    }

    @Test
    void unitRandomOneIsBasePlusVariation() {
        assertEquals(0.95, ScaleRoll.roll(Sex.FEMALE, defaults, 1.0), 1.0e-9);
        assertEquals(1.15, ScaleRoll.roll(Sex.MALE, defaults, 1.0), 1.0e-9);
    }

    @Test
    void unitRandomHalfIsTheBaseScale() {
        assertEquals(0.9, ScaleRoll.roll(Sex.FEMALE, defaults, 0.5), 1.0e-9);
        assertEquals(1.1, ScaleRoll.roll(Sex.MALE, defaults, 0.5), 1.0e-9);
    }

    @Test
    void wildAnimalsStayInTheSmallAndMediumBands() {
        SizeSettings size = new SizeSettings(0.8, 1.2, 0.1);

        assertEquals(0.70, ScaleRoll.rollWild(Sex.FEMALE, size, 0.0), 1.0e-9);
        assertEquals(1.10, ScaleRoll.rollWild(Sex.MALE, size, 0.0), 1.0e-9);
        assertEquals(SizeClass.S, SizeClass.of(Sex.FEMALE, ScaleRoll.rollWild(Sex.FEMALE, size, 0.0), size));
        assertEquals(SizeClass.M, SizeClass.of(Sex.FEMALE, ScaleRoll.rollWild(Sex.FEMALE, size, 1.0), size));
        assertEquals(SizeClass.M, SizeClass.of(Sex.MALE, ScaleRoll.rollWild(Sex.MALE, size, 1.0), size));
    }

    @Test
    void rollsStayInsideTheVanillaScaleAttribute() {
        SizeSettings extreme = new SizeSettings(0.1, 20.0, 1.0);
        assertEquals(0.0625, ScaleRoll.roll(Sex.FEMALE, extreme, 0.0), 1.0e-9);
        assertEquals(16.0, ScaleRoll.roll(Sex.MALE, extreme, 1.0), 1.0e-9);
    }
}
