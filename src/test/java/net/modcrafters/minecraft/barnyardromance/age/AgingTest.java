package net.modcrafters.minecraft.barnyardromance.age;

import net.modcrafters.minecraft.barnyardromance.text.English;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgingTest {
    private static final AgeSettings AGE = AgeSettings.DEFAULTS;

    @Test
    void defaultsAreADayYoungThirtyDaysToOldAndSixtyToDeath() {
        assertEquals(new AgeSettings(24_000L, 720_000L, 1_440_000L), AgeSettings.DEFAULTS);
    }

    @Test
    void babiesAreBabiesWhateverTheirAge() {
        assertEquals(AgeStage.BABY, Aging.stage(true, 0L, 24_000, 5_000_000L, AGE));
        assertEquals(AgeStage.BABY, Aging.stage(true, -1L, 24_000, 0L, AGE));
    }

    @Test
    void unknownBirthIsAlwaysAdult() {
        assertEquals(AgeStage.ADULT, Aging.stage(false, -1L, 24_000, 0L, AGE));
        assertEquals(AgeStage.ADULT, Aging.stage(false, -1L, 24_000, 50_000_000L, AGE));
    }

    @Test
    void aGrownAnimalStaysYoungForADay() {
        assertEquals(AgeStage.YOUNG, Aging.stage(false, 0L, 24_000, 24_000L, AGE));
        assertEquals(AgeStage.YOUNG, Aging.stage(false, 0L, 24_000, 47_999L, AGE));
        assertEquals(AgeStage.ADULT, Aging.stage(false, 0L, 24_000, 48_000L, AGE));
    }

    @Test
    void oldAgeStartsThirtyDaysAfterBirth() {
        assertEquals(AgeStage.ADULT, Aging.stage(false, 100L, 24_000, 720_099L, AGE));
        assertEquals(AgeStage.OLD, Aging.stage(false, 100L, 24_000, 720_100L, AGE));
        assertEquals(720_100L, Aging.oldFrom(100L, AGE));
    }

    @Test
    void oldBeatsYoungWhenTheyOverlap() {
        AgeSettings quick = new AgeSettings(100_000L, 50_000L, 0L);

        assertEquals(AgeStage.OLD, Aging.stage(false, 0L, 24_000, 60_000L, quick));
    }

    @Test
    void zeroTurnsEachStepOff() {
        AgeSettings off = new AgeSettings(0L, 0L, 0L);

        assertEquals(AgeStage.ADULT, Aging.stage(false, 0L, 24_000, 24_000L, off));
        assertEquals(AgeStage.ADULT, Aging.stage(false, 0L, 24_000, 50_000_000L, off));
        assertEquals(Long.MAX_VALUE, Aging.oldFrom(0L, off));
        assertEquals(Long.MAX_VALUE, Aging.deathAt(0L, off, 0.1, 0.5));
    }

    @Test
    void unknownBirthNeverGetsOldOrDies() {
        assertEquals(Long.MAX_VALUE, Aging.oldFrom(-1L, AGE));
        assertEquals(Long.MAX_VALUE, Aging.deathAt(-1L, AGE, 0.1, 0.5));
    }

    @Test
    void deathSpreadsBothWays() {
        assertEquals(1_440_000L, Aging.deathAt(0L, AGE, 0.1, 0.5));
        assertEquals(1_296_000L, Aging.deathAt(0L, AGE, 0.1, 0.0));
        assertEquals(1_584_000L, Aging.deathAt(0L, AGE, 0.1, 1.0));
        assertEquals(1_440_100L, Aging.deathAt(100L, AGE, 0.0, 0.0));
    }

    @Test
    void theSpreadValueStaysInTheUnitRange() {
        for (long bits : new long[]{0L, -1L, Long.MIN_VALUE, Long.MAX_VALUE, 0x5DEECE66DL, 12_345L}) {
            double value = Aging.spreadRandom(bits);
            assertTrue(value >= 0.0 && value < 1.0, "spread " + value + " for " + bits);
        }
    }

    @Test
    void onlyAdultsAndOldAnimalsBreed() {
        assertFalse(AgeStage.BABY.canBreed());
        assertFalse(AgeStage.YOUNG.canBreed());
        assertTrue(AgeStage.ADULT.canBreed());
        assertTrue(AgeStage.OLD.canBreed());
        assertEquals("Young", English.of(AgeStage.YOUNG.text()));
        assertEquals("Baby", English.of(AgeStage.BABY.text()));
        assertEquals("Adult", English.of(AgeStage.ADULT.text()));
        assertEquals("Old", English.of(AgeStage.OLD.text()));
    }
}
