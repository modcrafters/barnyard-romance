package net.modcrafters.minecraft.barnyardromance.drops;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeatDropTest {
    @Test
    void aSmallFemaleDropsTwoOrThree() {
        assertEquals(2, MeatDrop.count(0.70, 0.70, 1.30, 0.0));
        assertEquals(3, MeatDrop.count(0.70, 0.70, 1.30, 0.99));
    }

    @Test
    void anExtraLargeMaleDropsEightOrNine() {
        assertEquals(8, MeatDrop.count(1.30, 0.70, 1.30, 0.0));
        assertEquals(9, MeatDrop.count(1.30, 0.70, 1.30, 0.99));
    }

    @Test
    void lootingStacksOnTopOfTheSizedAmount() {
        assertEquals(11, MeatDrop.resizedCount("minecraft:beef", 5, 1.30, 0.70, 1.30, 0.99));
    }

    @Test
    void leatherIsLeftAlone() {
        assertEquals(2, MeatDrop.resizedCount("minecraft:leather", 2, 1.30, 0.70, 1.30, 0.99));
    }
}
