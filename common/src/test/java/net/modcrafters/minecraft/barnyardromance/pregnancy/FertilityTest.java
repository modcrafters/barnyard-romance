package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FertilityTest {
    @Test
    void aBirthCostsAQuarter() {
        assertEquals(0.75, Fertility.afterBirth(1.0), 1.0e-9);
        assertEquals(0.0, Fertility.afterBirth(0.1), 1.0e-9);
    }

    @Test
    void fertilityRecoversATenthADay() {
        assertEquals(0.6, Fertility.current(0.5, 0L, 24_000L), 1.0e-9);
        assertEquals(0.55, Fertility.current(0.5, 0L, 12_000L), 1.0e-9);
    }

    @Test
    void recoveryCapsAtFullAndIgnoresAClockThatRunsBackwards() {
        assertEquals(1.0, Fertility.current(0.5, 0L, 24_000L * 100), 1.0e-9);
        assertEquals(0.5, Fertility.current(0.5, 1_000L, 0L), 1.0e-9);
        assertEquals(0.8, Fertility.current(0.8, -1L, 99L), 1.0e-9);
    }

    @Test
    void aQuarterIsEnoughToConceive() {
        assertTrue(Fertility.canConceive(0.25));
        assertFalse(Fertility.canConceive(0.2499));
    }

    @Test
    void aWornOutMotherRecoversInADay() {
        AnimalTraits traits = new AnimalTraits(Sex.FEMALE, 1.0).withFertility(0.2, 0L);

        assertTrue(Fertility.wornOut(traits, 0L));
        assertFalse(Fertility.wornOut(traits, 24_000L));
    }
}
