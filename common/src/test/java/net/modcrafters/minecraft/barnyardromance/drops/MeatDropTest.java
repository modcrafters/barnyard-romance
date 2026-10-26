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
        assertEquals(11, MeatDrop.resizedCount("minecraft:beef", 5, 1.30, 0.70, 1.30, 1.0, 0.99, 0.0));
    }

    @Test
    void leatherIsLeftAlone() {
        assertEquals(2, MeatDrop.resizedCount("minecraft:leather", 2, 1.30, 0.70, 1.30, 1.25, 0.99, 0.0));
    }

    @Test
    void factorScalesTheCurveButNotLooting() {
        // 8 from the curve × 1.25 = 10, plus 2 from looting.
        assertEquals(12, MeatDrop.resizedCount("minecraft:beef", 5, 1.30, 0.70, 1.30, 1.25, 0.0, 0.5));
        // 3 × 0.75 = 2.25: rounds up when the second random falls under the quarter.
        assertEquals(3, MeatDrop.resizedCount("minecraft:beef", 3, 0.70, 0.70, 1.30, 0.75, 0.99, 0.2));
        assertEquals(2, MeatDrop.resizedCount("minecraft:beef", 3, 0.70, 0.70, 1.30, 0.75, 0.99, 0.3));
    }
}
