package net.modcrafters.minecraft.barnyardromance.sex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InheritanceTest {
    @Test
    void anUnluckyBabyMatchesTheParentsAverage() {
        assertEquals(0.5, Inheritance.child(0.0, 1.0, 0.99, 0.5, 0.0), 1.0e-9);
    }

    @Test
    void aLuckyBabyStepsUp() {
        assertEquals(0.65, Inheritance.child(0.4, 0.4, 0.0, 0.5, 0.0), 1.0e-9);
        assertEquals(0.25, Inheritance.child(0.0, 0.0, 0.0, 0.5, 0.0), 1.0e-9);
    }

    @Test
    void goodFoodMakesAnAverageRollCountAsLucky() {
        assertEquals(0.25, Inheritance.child(0.0, 0.5, 0.5, 0.5, 0.0), 1.0e-9);
        assertEquals(0.5, Inheritance.child(0.0, 0.5, 0.5, 0.5, 1.0), 1.0e-9);
    }

    @Test
    void theSpreadNudgesBothWays() {
        assertEquals(0.45, Inheritance.child(0.5, 0.5, 0.99, 0.0, 0.0), 1.0e-9);
        assertEquals(0.55, Inheritance.child(0.5, 0.5, 0.99, 1.0, 0.0), 1.0e-9);
    }

    @Test
    void spreadNeverLeavesTheUnitRange() {
        assertEquals(1.0, Inheritance.child(1.0, 1.0, 0.0, 1.0, 1.0), 1.0e-9);
        assertEquals(0.0, Inheritance.child(0.0, 0.0, 0.99, 0.0, 0.0), 1.0e-9);
    }
}
