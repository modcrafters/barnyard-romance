package net.modcrafters.minecraft.barnyardromance.sex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GrowthTest {
    @Test
    void theDefaultSitsInsideTheFastAndSlowEnds() {
        assertEquals(24_000, Growth.wild());
        assertEquals(15_000, Growth.fromPercent(1.0));
        assertEquals(30_000, Growth.fromPercent(0.0));
        assertEquals(0.4, Growth.percent(Growth.wild()), 1.0e-9);
    }

    @Test
    void aBabyAveragesItsParentsAndCanGrowFaster() {
        assertEquals(24_000, Growth.child(24_000, 24_000, 1.0, 0.0));
        assertEquals(20_250, Growth.child(24_000, 24_000, 0.0, 0.0));
        assertEquals(15_000, Growth.child(15_000, 15_000, 0.0, 1.0));
        assertEquals(26_250, Growth.child(30_000, 30_000, 0.0, 0.0));
    }

    @Test
    void growthTimeReadsAsAClock() {
        assertEquals("20m 0s", Growth.clock(24_000));
        assertEquals("12m 30s", Growth.clock(15_000));
    }
}
