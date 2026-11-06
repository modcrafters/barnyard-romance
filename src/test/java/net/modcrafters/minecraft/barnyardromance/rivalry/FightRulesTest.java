package net.modcrafters.minecraft.barnyardromance.rivalry;

import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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

    @Test
    void eachMaleRollsAtHisOwnTimeOfDay() {
        UUID id = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e");
        int time = FightRules.rollTime(id);
        assertTrue(time >= 0 && time < 24_000, "roll time " + time);
        assertEquals(time, FightRules.rollTime(UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e")));
        assertTrue(FightRules.rollTime(new UUID(0L, -1L)) >= 0);
    }

    @Test
    void roostersAlwaysMightFightButOnlyTwoWildMalesOtherwise() {
        assertEquals(0.25, FightRules.randomChance(true, false, false, Tuning.DEFAULTS), 1.0e-9);
        assertEquals(0.25, FightRules.randomChance(true, true, true, Tuning.DEFAULTS), 1.0e-9);
        assertEquals(0.05, FightRules.randomChance(false, true, true, Tuning.DEFAULTS), 1.0e-9);
        assertEquals(0.0, FightRules.randomChance(false, true, false, Tuning.DEFAULTS), 1.0e-9);
        assertEquals(0.0, FightRules.randomChance(false, false, false, Tuning.DEFAULTS), 1.0e-9);
    }

    @Test
    void aMaleRollsOnceADayAfterHisTime() {
        assertTrue(FightRules.due(4L, 5L, 12_000, 6_000));
        assertFalse(FightRules.due(4L, 5L, 5_999, 6_000));
        assertFalse(FightRules.due(5L, 5L, 20_000, 6_000));
        assertTrue(FightRules.due(-1L, 0L, 6_000, 6_000));
    }
}
