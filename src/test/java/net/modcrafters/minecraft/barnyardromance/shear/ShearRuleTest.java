package net.modcrafters.minecraft.barnyardromance.shear;

import net.modcrafters.minecraft.barnyardromance.shear.api.ShearState;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;

class ShearRuleTest {
    private static final AnimalTraits EWE = AnimalTraits.of(Sex.FEMALE, 1.0).with(CARE, FamiliarityState.NONE.fed(0.8, -1L));

    @Test
    void aSheepNeverShearedHasNotBeenShearedToday() {
        assertFalse(ShearRule.shearedToday(EWE, 5_000L));
    }

    // Like feeding and milking, the day turns over at midnight on the overworld clock (18,000).
    @Test
    void shearingLastsUntilMidnight() {
        AnimalTraits sheared = EWE.with(ShearSlices.LAST_SHEARED, 10_000L);

        assertTrue(ShearRule.shearedToday(sheared, 17_999L));
        assertFalse(ShearRule.shearedToday(sheared, 18_000L));
    }

    @Test
    void theBookSaysWhetherASheepCanBeShorn() {
        long clock = 10_000L;

        assertEquals(ShearState.READY, ShearRule.state(true, true, false, EWE, clock));
        assertEquals(ShearState.SHEARED_TODAY, ShearRule.state(true, true, false, EWE.with(ShearSlices.LAST_SHEARED, clock), clock));
        assertEquals(ShearState.REGROWING, ShearRule.state(true, false, false, EWE, clock));
        assertEquals(ShearState.NONE, ShearRule.state(false, true, false, EWE, clock));
        assertEquals(ShearState.NONE, ShearRule.state(true, true, true, EWE, clock));
    }
}
