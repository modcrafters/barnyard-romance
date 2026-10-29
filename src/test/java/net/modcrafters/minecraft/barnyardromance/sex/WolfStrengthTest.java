package net.modcrafters.minecraft.barnyardromance.sex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WolfStrengthTest {
    // The default sizes: females 0.7 to 0.9, males 1.1 to 1.3.
    private final SizeSettings size = new SizeSettings(0.8, 1.2, 0.1);

    @Test
    void theSmallestFemaleBitesLikeAStickWithHalfTheHealth() {
        assertEquals(0.25, WolfStrength.attack(0.7, this.size), 1.0e-9);
        assertEquals(0.5, WolfStrength.health(0.7, this.size), 1.0e-9);
    }

    @Test
    void theLargestMaleBitesLikeANetheriteSwordWithHalfAgainTheHealth() {
        assertEquals(2.0, WolfStrength.attack(1.3, this.size), 1.0e-9);
        assertEquals(1.5, WolfStrength.health(1.3, this.size), 1.0e-9);
    }

    @Test
    void aWolfHalfwayBetweenIsAVanillaWolf() {
        assertEquals(1.0, WolfStrength.attack(1.0, this.size), 1.0e-9);
        assertEquals(1.0, WolfStrength.health(1.0, this.size), 1.0e-9);
    }

    @Test
    void typicalFemalesAndMalesAreFarApart() {
        assertEquals(0.5, WolfStrength.attack(0.8, this.size), 1.0e-9);
        assertEquals(2.0 / 3.0, WolfStrength.health(0.8, this.size), 1.0e-9);
        assertEquals(5.0 / 3.0, WolfStrength.attack(1.2, this.size), 1.0e-9);
        assertEquals(4.0 / 3.0, WolfStrength.health(1.2, this.size), 1.0e-9);
    }

    @Test
    void sizesOutsideTheConfigStopAtTheEnds() {
        assertEquals(0.25, WolfStrength.attack(0.1, this.size), 1.0e-9);
        assertEquals(2.0, WolfStrength.attack(5.0, this.size), 1.0e-9);
        assertEquals(1.5, WolfStrength.health(5.0, this.size), 1.0e-9);
    }

    @Test
    void theScaleStretchesWithTheConfig() {
        SizeSettings big = new SizeSettings(1.6, 2.4, 0.2);

        assertEquals(0.25, WolfStrength.attack(1.4, big), 1.0e-9);
        assertEquals(1.0, WolfStrength.attack(2.0, big), 1.0e-9);
        assertEquals(2.0, WolfStrength.attack(2.6, big), 1.0e-9);
    }

    @Test
    void wolvesAllOneSizeAreVanilla() {
        SizeSettings same = new SizeSettings(1.0, 1.0, 0.0);

        assertEquals(1.0, WolfStrength.attack(1.0, same), 1.0e-9);
        assertEquals(1.0, WolfStrength.health(1.0, same), 1.0e-9);
    }
}
