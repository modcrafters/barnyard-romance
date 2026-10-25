package net.modcrafters.minecraft.barnyardromance.pregnancy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PregnancyTextTest {
    @Test
    void aFullDayLeftIsTwentyMinutes() {
        assertEquals("Pregnant 0% · 20m 0s", PregnancyText.describe(0.0, 24_000));
    }

    @Test
    void halfwayIsTenMinutes() {
        assertEquals("Pregnant 50% · 10m 0s", PregnancyText.describe(0.5, 12_000));
    }

    @Test
    void underAMinuteDropsTheMinuteField() {
        assertEquals("Pregnant 99% · 1s", PregnancyText.describe(0.99, 30));
    }

    @Test
    void noTimeLeftSaysDue() {
        assertEquals("Pregnant, due", PregnancyText.describe(1.0, 0));
    }

    @Test
    void theCardClockDropsThePregnantWord() {
        assertEquals("10m 0s", PregnancyText.clock(12_000));
        assertEquals("due", PregnancyText.clock(0));
    }

    @Test
    void theInfoLineNamesTheFather() {
        Pregnancy pregnancy = new Pregnancy(1_000, 24_000, "minecraft:cow", 1.1, 1, 0.0);

        assertEquals("Pregnant 50% · 10m 0s", PregnancyText.line(pregnancy, 13_000, false));
        assertEquals("Pregnant 50% · 10m 0s · father minecraft:cow", PregnancyText.line(pregnancy, 13_000, true));
    }
}
