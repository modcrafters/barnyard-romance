package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
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
    void milkingCanBeTurnedOff() {
        Tuning off = BarnyardConfig.parse("""
                { "tuning": { "milkingsPerDaySmall": 0, "milkingsPerDayLarge": 0 } }
                """).tuning();

        assertEquals(MilkRules.Check.DISABLED, MilkRules.check(Sex.FEMALE, SizeClass.XL, MOTHER, 100L, 100L, off));
        assertEquals("Milking is turned off.", MilkRules.text(MilkRules.Check.DISABLED));
    }

    @Test
    void malesNeverMilk() {
        assertEquals(MilkRules.Check.MALE, MilkRules.check(Sex.MALE, SizeClass.XL, MOTHER, 100L, 100L, TUNING));
    }

    @Test
    void smallAndMediumMilkOnce() {
        Lactation milked = MOTHER.milked(100L, 100L, 72_000L);

        assertEquals(1, MilkRules.perDay(SizeClass.S, TUNING));
        assertEquals(1, MilkRules.perDay(SizeClass.M, TUNING));
        assertEquals(MilkRules.Check.OK, MilkRules.check(Sex.FEMALE, SizeClass.M, MOTHER, 100L, 100L, TUNING));
        assertEquals(MilkRules.Check.ENOUGH_TODAY, MilkRules.check(Sex.FEMALE, SizeClass.M, milked, 200L, 200L, TUNING));
    }

    @Test
    void largeAndExtraLargeMilkTwice() {
        Lactation milked = MOTHER.milked(100L, 100L, 72_000L);

        assertEquals(2, MilkRules.perDay(SizeClass.L, TUNING));
        assertEquals(2, MilkRules.perDay(SizeClass.XL, TUNING));
        assertEquals(MilkRules.Check.OK, MilkRules.check(Sex.FEMALE, SizeClass.L, milked, 200L, 200L, TUNING));
        assertEquals(MilkRules.Check.ENOUGH_TODAY, MilkRules.check(Sex.FEMALE, SizeClass.L, milked.milked(200L, 200L, 72_000L), 300L, 300L, TUNING));
    }

    @Test
    void dryFemaleHasNoMilk() {
        assertEquals(MilkRules.Check.NO_MILK, MilkRules.check(Sex.FEMALE, SizeClass.XL, Lactation.NONE, 100L, 100L, TUNING));
    }

    // A baby is marked at its first join, so it never gets the window when it grows up.
    @Test
    void bornAnimalsGetNoGrace() {
        Lactation baby = MilkRules.firstJoin(Sex.FEMALE, true, -1L, Lactation.NONE, 1_000L, TUNING);

        assertTrue(baby.graceGiven());
        assertFalse(baby.hasMilk(1_000L, TUNING.milkKeepsFlowingTicks()));
        assertEquals(baby, MilkRules.firstJoin(Sex.FEMALE, false, -1L, baby, 50_000L, TUNING));
    }

    // Wild adults, and adults bred before Stage 5 that never calved, get one window.
    @Test
    void wildAdultFemalesGetGrace() {
        Lactation wild = MilkRules.firstJoin(Sex.FEMALE, false, -1L, Lactation.NONE, 1_000L, TUNING);

        assertTrue(wild.graceGiven());
        assertTrue(wild.hasMilk(1_000L + 72_000L - 1L, TUNING.milkKeepsFlowingTicks()));
        assertFalse(wild.hasMilk(1_000L + 72_000L, TUNING.milkKeepsFlowingTicks()));
    }

    @Test
    void malesGetNoGrace() {
        Lactation male = MilkRules.firstJoin(Sex.MALE, false, -1L, Lactation.NONE, 1_000L, TUNING);

        assertTrue(male.graceGiven());
        assertFalse(male.hasMilk(1_000L, TUNING.milkKeepsFlowingTicks()));
    }

    // A mother from before Stage 5 has her milk worked out from her recorded birth.
    @Test
    void oldMothersMilkFromTheirLastBirth() {
        Lactation recent = MilkRules.firstJoin(Sex.FEMALE, false, 90_000L, Lactation.NONE, 100_000L, TUNING);
        Lactation longAgo = MilkRules.firstJoin(Sex.FEMALE, false, 1_000L, Lactation.NONE, 500_000L, TUNING);

        assertTrue(recent.nursing(100_000L));
        assertTrue(recent.hasMilk(100_000L, TUNING.milkKeepsFlowingTicks()));
        assertTrue(longAgo.graceGiven());
        assertFalse(longAgo.hasMilk(500_000L, TUNING.milkKeepsFlowingTicks()));
    }
}
