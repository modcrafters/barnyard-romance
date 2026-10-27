package net.modcrafters.minecraft.barnyardromance.sex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SizeClassTest {
    private final SizeSettings size = new SizeSettings(0.8, 1.2, 0.1);

    @Test
    void femalesSplitFromSmallToExtraLarge() {
        assertEquals(SizeClass.S, SizeClass.of(Sex.FEMALE, 0.70, size));
        assertEquals(SizeClass.S, SizeClass.of(Sex.FEMALE, 0.749, size));
        assertEquals(SizeClass.M, SizeClass.of(Sex.FEMALE, 0.75, size));
        assertEquals(SizeClass.L, SizeClass.of(Sex.FEMALE, 0.80, size));
        assertEquals(SizeClass.XL, SizeClass.of(Sex.FEMALE, 0.85, size));
        assertEquals(SizeClass.XL, SizeClass.of(Sex.FEMALE, 0.90, size));
    }

    @Test
    void malesUseTheirOwnRange() {
        assertEquals(SizeClass.S, SizeClass.of(Sex.MALE, 1.10, size));
        assertEquals(SizeClass.M, SizeClass.of(Sex.MALE, 1.15, size));
        assertEquals(SizeClass.L, SizeClass.of(Sex.MALE, 1.20, size));
        assertEquals(SizeClass.XL, SizeClass.of(Sex.MALE, 1.25, size));
        assertEquals(SizeClass.XL, SizeClass.of(Sex.MALE, 1.30, size));
    }

    @Test
    void aScaleOutsideTheRangeStaysOnTheNearestLetter() {
        assertEquals(SizeClass.S, SizeClass.of(Sex.FEMALE, 0.40, size));
        assertEquals(SizeClass.XL, SizeClass.of(Sex.MALE, 2.0, size));
    }
}
