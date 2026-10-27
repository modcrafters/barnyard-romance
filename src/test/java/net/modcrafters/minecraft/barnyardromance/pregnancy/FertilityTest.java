package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.age.AgeSettings;
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

        assertTrue(Fertility.wornOut(traits, AgeSettings.DEFAULTS, 0L));
        assertFalse(Fertility.wornOut(traits, AgeSettings.DEFAULTS, 24_000L));
    }

    @Test
    void oldAgeHalvesRecoveryFromTheOldStart() {
        assertEquals(0.6, Fertility.current(0.5, 0L, 24_000L, Long.MAX_VALUE), 1.0e-9);
        assertEquals(0.55, Fertility.current(0.5, 0L, 24_000L, 0L), 1.0e-9);
        assertEquals(0.575, Fertility.current(0.5, 0L, 24_000L, 12_000L), 1.0e-9);
        assertEquals(0.525, Fertility.current(0.5, 12_000L, 24_000L, 0L), 1.0e-9);
    }

    @Test
    void anOldMotherTakesTwoDaysToRecoverWhatAYoungOneDoesInOne() {
        AgeSettings age = AgeSettings.DEFAULTS;
        AnimalTraits old = new AnimalTraits(Sex.FEMALE, 1.0).withBornTick(0L).withFertility(0.2, 800_000L);

        assertEquals(0.25, Fertility.current(old, age, 824_000L), 1.0e-9);
        assertEquals(0.3, Fertility.current(old, age, 848_000L), 1.0e-9);
    }
}
