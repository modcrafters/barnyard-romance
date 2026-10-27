package net.modcrafters.minecraft.barnyardromance.sex;

import net.modcrafters.minecraft.barnyardromance.text.English;
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
    void growthTimeReadsAsAClock() {
        assertEquals("20m 0s", English.of(Growth.clock(24_000)));
        assertEquals("12m 30s", English.of(Growth.clock(15_000)));
        assertEquals("45s", English.of(Growth.clock(900)));
    }
}
