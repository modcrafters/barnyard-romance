package net.modcrafters.minecraft.barnyardromance.rivalry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FightRulesTest {
    @Test
    void biggerMalesHitHarder() {
        assertEquals(2.6F, FightRules.clashDamage(1.3), 1.0e-4F);
        assertEquals(1.4F, FightRules.clashDamage(0.7), 1.0e-4F);
        assertEquals(2.0F, FightRules.clashDamage(1.0), 1.0e-4F);
    }

    @Test
    void foodStopsOnlyAFightBetweenTwoFamiliarMales() {
        assertTrue(FightRules.stops(true, true, true));
        assertFalse(FightRules.stops(true, false, true));
        assertFalse(FightRules.stops(false, true, true));
        assertFalse(FightRules.stops(true, true, false));
    }
}
