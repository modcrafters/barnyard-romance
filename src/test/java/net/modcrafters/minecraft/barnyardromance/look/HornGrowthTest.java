package net.modcrafters.minecraft.barnyardromance.look;

import net.modcrafters.minecraft.barnyardromance.age.AgeSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HornGrowthTest {
    private static final AgeSettings AGE = AgeSettings.DEFAULTS;
    private static final int GROWTH = 24_000;

    @Test
    void babiesHaveNoHorns() {
        assertEquals(0.0, HornGrowth.size(true, 0L, GROWTH, 10_000L, AGE), 1.0e-9);
        assertEquals(0.0, HornGrowth.size(true, -1L, GROWTH, 10_000L, AGE), 1.0e-9);
    }

    @Test
    void unknownBirthHasFullHorns() {
        assertEquals(1.0, HornGrowth.size(false, -1L, GROWTH, 0L, AGE), 1.0e-9);
    }

    @Test
    void noYoungStretchMeansFullHornsOnceGrown() {
        AgeSettings noYoung = new AgeSettings(0L, AGE.oldAfterTicks(), AGE.deathAfterTicks());

        assertEquals(1.0, HornGrowth.size(false, 0L, GROWTH, 24_000L, noYoung), 1.0e-9);
    }

    @Test
    void hornsAppearAtAFifthWhenTheAnimalTurnsYoung() {
        assertEquals(0.2, HornGrowth.size(false, 0L, GROWTH, 24_000L, AGE), 1.0e-9);
    }

    @Test
    void hornsGrowInAStraightLineWhileYoung() {
        assertEquals(0.6, HornGrowth.size(false, 0L, GROWTH, 36_000L, AGE), 1.0e-9);
    }

    @Test
    void adultsHaveFullHorns() {
        assertEquals(1.0, HornGrowth.size(false, 0L, GROWTH, 48_000L, AGE), 1.0e-9);
        assertEquals(1.0, HornGrowth.size(false, 0L, GROWTH, 5_000_000L, AGE), 1.0e-9);
    }

    @Test
    void anAnimalThatGrewUpEarlyStartsAtAFifth() {
        // Fed babies grow up before their growth time runs out.
        assertEquals(0.2, HornGrowth.size(false, 0L, GROWTH, 10_000L, AGE), 1.0e-9);
    }
}
