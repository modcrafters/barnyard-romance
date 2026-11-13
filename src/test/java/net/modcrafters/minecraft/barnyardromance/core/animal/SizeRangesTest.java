package net.modcrafters.minecraft.barnyardromance.core.animal;

import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SizeRangesTest {
    private static final SizeSettings OLD = new SizeSettings(0.8, 1.2, 0.1);
    private static final SizeSettings NEW = new SizeSettings(0.87, 1.13, 0.1);

    @Test
    void aMaleMovesToTheSameSpotInTheNewRange() {
        assertEquals(1.09, SizeRanges.rescale(Sex.MALE, 1.16, OLD, NEW), 1e-9);
    }

    @Test
    void aFemaleMovesToTheSameSpotInTheNewRange() {
        assertEquals(0.82, SizeRanges.rescale(Sex.FEMALE, 0.75, OLD, NEW), 1e-9);
    }

    @Test
    void aWiderVariationStretchesTheOffset() {
        assertEquals(0.9, SizeRanges.rescale(Sex.FEMALE, 0.85, OLD, new SizeSettings(0.8, 1.2, 0.2)), 1e-9);
    }

    @Test
    void anAnimalOutsideItsRangeStaysAsFarOutside() {
        assertEquals(1.33, SizeRanges.rescale(Sex.MALE, 1.40, OLD, NEW), 1e-9);
    }

    @Test
    void fromZeroVariationGoesToTheBase() {
        assertEquals(1.13, SizeRanges.rescale(Sex.MALE, 1.25, new SizeSettings(0.8, 1.2, 0.0), NEW), 1e-9);
    }

    @Test
    void toZeroVariationGoesToTheBase() {
        assertEquals(0.87, SizeRanges.rescale(Sex.FEMALE, 0.75, OLD, new SizeSettings(0.87, 1.13, 0.0)), 1e-9);
    }

    @Test
    void theResultIsClampedToTheScaleLimits() {
        assertEquals(16.0, SizeRanges.rescale(Sex.MALE, 15.0, OLD, new SizeSettings(8.0, 12.0, 1.0)), 1e-9);
    }

    @Test
    void everyBandIsKept() {
        // the middle of each band, both sexes
        for (Sex sex : Sex.values()) {
            for (double t : new double[] {0.125, 0.375, 0.625, 0.875}) {
                double scale = SizeRanges.base(sex, OLD) - 0.1 + t * 0.2;
                assertEquals(SizeClass.of(sex, scale, OLD), SizeClass.of(sex, SizeRanges.rescale(sex, scale, OLD, NEW), NEW));
            }
        }
    }

    @Test
    void settledFillsAMissingRangeWithoutMoving() {
        AnimalTraits settled = SizeRanges.settled(AnimalTraits.of(Sex.MALE, 1.16), NEW);

        assertEquals(1.16, settled.scale(), 1e-9);
        assertEquals(Optional.of(NEW), settled.sizeRange());
    }

    @Test
    void settledLeavesTheSameRangeAlone() {
        AnimalTraits traits = AnimalTraits.of(Sex.MALE, 1.16).withSizeRange(NEW);

        assertSame(traits, SizeRanges.settled(traits, NEW));
    }

    @Test
    void settledRescalesADifferentRange() {
        AnimalTraits settled = SizeRanges.settled(AnimalTraits.of(Sex.MALE, 1.16).withSizeRange(OLD), NEW);

        assertEquals(1.09, settled.scale(), 1e-9);
        assertEquals(Optional.of(NEW), settled.sizeRange());
    }
}
