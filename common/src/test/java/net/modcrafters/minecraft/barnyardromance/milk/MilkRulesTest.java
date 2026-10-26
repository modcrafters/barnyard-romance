package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MilkRulesTest {
    private static final Tuning TUNING = Tuning.DEFAULTS;
    private static final Lactation MOTHER = Lactation.NONE.afterBirth(0L, 24_000L, 72_000L);

    @Test
    void malesNeverMilk() {
        assertEquals(MilkRules.Check.MALE, MilkRules.check(Sex.MALE, SizeClass.XL, MOTHER, 100L, TUNING));
    }

    @Test
    void smallAndMediumMilkOnce() {
        Lactation milked = MOTHER.milked(100L, 72_000L);

        assertEquals(1, MilkRules.perDay(SizeClass.S, TUNING));
        assertEquals(1, MilkRules.perDay(SizeClass.M, TUNING));
        assertEquals(MilkRules.Check.OK, MilkRules.check(Sex.FEMALE, SizeClass.M, MOTHER, 100L, TUNING));
        assertEquals(MilkRules.Check.ENOUGH_TODAY, MilkRules.check(Sex.FEMALE, SizeClass.M, milked, 200L, TUNING));
    }

    @Test
    void largeAndExtraLargeMilkTwice() {
        Lactation milked = MOTHER.milked(100L, 72_000L);

        assertEquals(2, MilkRules.perDay(SizeClass.L, TUNING));
        assertEquals(2, MilkRules.perDay(SizeClass.XL, TUNING));
        assertEquals(MilkRules.Check.OK, MilkRules.check(Sex.FEMALE, SizeClass.L, milked, 200L, TUNING));
        assertEquals(MilkRules.Check.ENOUGH_TODAY, MilkRules.check(Sex.FEMALE, SizeClass.L, milked.milked(200L, 72_000L), 300L, TUNING));
    }

    @Test
    void dryFemaleHasNoMilk() {
        assertEquals(MilkRules.Check.NO_MILK, MilkRules.check(Sex.FEMALE, SizeClass.XL, Lactation.NONE, 100L, TUNING));
    }

    @Test
    void bornAnimalsGetNoGrace() {
        assertFalse(MilkRules.getsGrace(Sex.FEMALE, false, 5_000L, -1L));
        assertFalse(MilkRules.getsGrace(Sex.FEMALE, false, -1L, 5_000L));
    }

    @Test
    void wildAdultFemalesGetGrace() {
        assertTrue(MilkRules.getsGrace(Sex.FEMALE, false, -1L, -1L));
        assertFalse(MilkRules.getsGrace(Sex.FEMALE, true, -1L, -1L));
        assertFalse(MilkRules.getsGrace(Sex.MALE, false, -1L, -1L));
    }
}
