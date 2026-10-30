package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.config.EggSettings;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EggRulesTest {
    @Test
    void perDayBySize() {
        assertEquals(1, EggRules.perDay(SizeClass.S, false, false, EggSettings.CHICKEN));
        assertEquals(1, EggRules.perDay(SizeClass.M, false, false, EggSettings.CHICKEN));
        assertEquals(2, EggRules.perDay(SizeClass.L, false, false, EggSettings.CHICKEN));
        assertEquals(2, EggRules.perDay(SizeClass.XL, false, false, EggSettings.CHICKEN));
    }

    @Test
    void happyAddsOne() {
        assertEquals(2, EggRules.perDay(SizeClass.S, false, true, EggSettings.CHICKEN));
        assertEquals(3, EggRules.perDay(SizeClass.XL, false, true, EggSettings.CHICKEN));
    }

    // A quarter of an in-game hour (1,000 ticks), and five minutes more for each egg after the first.
    @Test
    void sittingIsFifteenMinutesAndFiveMorePerEgg() {
        assertEquals(250, EggRules.sitTicks(1));
        assertEquals(333, EggRules.sitTicks(2));
        assertEquals(416, EggRules.sitTicks(3));
    }

    @Test
    void hungryYesterdayLaysNothing() {
        assertEquals(0, EggRules.perDay(SizeClass.XL, true, true, EggSettings.CHICKEN));
    }

    @Test
    void disabledLaysNothing() {
        assertEquals(0, EggRules.perDay(SizeClass.XL, false, true, EggSettings.NONE));
    }

    @Test
    void thrownChickIsSmall() {
        assertEquals(SizeClass.S, EggRules.thrownChickSize(SizeClass.M, SizeClass.M, 0.0));
        assertEquals(SizeClass.M, EggRules.thrownChickSize(SizeClass.L, SizeClass.S, 0.05));
        assertEquals(SizeClass.S, EggRules.thrownChickSize(SizeClass.L, SizeClass.S, 0.5));
        assertEquals(SizeClass.M, EggRules.thrownChickSize(SizeClass.S, SizeClass.XL, 0.05));
    }
}
