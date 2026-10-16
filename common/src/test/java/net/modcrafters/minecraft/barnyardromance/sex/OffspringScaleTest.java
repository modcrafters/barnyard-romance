package net.modcrafters.minecraft.barnyardromance.sex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OffspringScaleTest {
    private final SizeSettings size = new SizeSettings(0.8, 1.2, 0.1);

    @Test
    void anUnluckyBabyMatchesTheParentsAverage() {
        double female = OffspringScale.childScale(Sex.FEMALE, 0.70, 1.30, size, 0.99, 0.0);
        double male = OffspringScale.childScale(Sex.MALE, 0.70, 1.30, size, 0.99, 0.0);

        assertEquals(0.80, female, 1.0e-9);
        assertEquals(1.20, male, 1.0e-9);
    }

    @Test
    void aLuckyBabyFromSmallParentsStepsUpOneBand() {
        double female = OffspringScale.childScale(Sex.FEMALE, 0.70, 1.10, size, 0.0, 0.0);

        assertEquals(0.75, female, 1.0e-9);
        assertEquals(SizeClass.M, SizeClass.of(Sex.FEMALE, female, size));
    }

    @Test
    void goodFoodMakesAnAverageRollCountAsLucky() {
        double hungry = OffspringScale.childScale(Sex.FEMALE, 0.70, 1.10, size, 0.50, 0.0);
        double fed = OffspringScale.childScale(Sex.FEMALE, 0.70, 1.10, size, 0.50, 1.0);

        assertEquals(0.70, hungry, 1.0e-9);
        assertEquals(0.75, fed, 1.0e-9);
    }

    @Test
    void improvementStopsAtTheTopOfTheRange() {
        double female = OffspringScale.childScale(Sex.FEMALE, 0.90, 1.30, size, 0.0, 1.0);

        assertEquals(0.90, female, 1.0e-9);
        assertEquals(SizeClass.XL, SizeClass.of(Sex.FEMALE, female, size));
    }
}