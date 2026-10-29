package net.modcrafters.minecraft.barnyardromance.shear;

import net.modcrafters.minecraft.barnyardromance.herd.ShearState;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShearRuleTest {
    private static final AnimalTraits EWE = new AnimalTraits(Sex.FEMALE, 1.0, 0.8, -1L);

    @Test
    void aSheepNeverShearedHasNotBeenShearedToday() {
        assertFalse(ShearRule.shearedToday(EWE, 5_000L));
    }

    // Like feeding and milking, the day turns over at midnight on the overworld clock (18,000).
    @Test
    void shearingLastsUntilMidnight() {
        AnimalTraits sheared = EWE.sheared(10_000L);

        assertTrue(ShearRule.shearedToday(sheared, 17_999L));
        assertFalse(ShearRule.shearedToday(sheared, 18_000L));
    }

    @Test
    void theBookSaysWhetherASheepCanBeShorn() {
        long clock = 10_000L;

        assertEquals(ShearState.READY, ShearRule.state(true, true, false, EWE, clock));
        assertEquals(ShearState.SHEARED_TODAY, ShearRule.state(true, true, false, EWE.sheared(clock), clock));
        assertEquals(ShearState.REGROWING, ShearRule.state(true, false, false, EWE, clock));
        assertEquals(ShearState.NONE, ShearRule.state(false, true, false, EWE, clock));
        assertEquals(ShearState.NONE, ShearRule.state(true, true, true, EWE, clock));
    }
}
