package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.core.config.HungerSettings;
import net.modcrafters.minecraft.barnyardromance.core.config.HungerSettings.CatchUp;
import net.modcrafters.minecraft.barnyardromance.core.config.Tuning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DailyUpkeepTest {
    private static final HungerSettings HUNGER = HungerSettings.DEFAULTS;
    private static final Tuning TUNING = Tuning.DEFAULTS;

    private static DailyUpkeep.State state(double familiarity, float health, boolean cared, boolean grown, boolean pet) {
        return new DailyUpkeep.State(familiarity, health, cared, grown, pet);
    }

    @Test
    void aFedDayChangesNothing() {
        DailyUpkeep.Outcome outcome = DailyUpkeep.day(state(0.5, 10, true, true, false), true, 0.0, HUNGER, TUNING);
        assertEquals(0.5, outcome.familiarity(), 1e-9);
        assertEquals(0f, outcome.damage());
        assertFalse(outcome.escapes());
    }

    @Test
    void aHungryFamiliarDayCostsFamiliarityAndHalfAHeart() {
        DailyUpkeep.Outcome outcome = DailyUpkeep.day(state(0.5, 10, true, true, true), false, 0.9, HUNGER, TUNING);
        assertEquals(0.45, outcome.familiarity(), 1e-9);
        assertEquals(1.0f, outcome.damage(), 1e-6);
    }

    @Test
    void hungerNeverGoesBelowMinHealth() {
        assertEquals(0.5f, DailyUpkeep.day(state(0.5, 1.5f, true, true, true), false, 0.9, HUNGER, TUNING).damage(), 1e-6);
        assertEquals(0f, DailyUpkeep.day(state(0.5, 1.0f, true, true, true), false, 0.9, HUNGER, TUNING).damage());
    }

    @Test
    void minHealthZeroLetsHungerKill() {
        HungerSettings deadly = new HungerSettings(true, 0.0, 1.0, CatchUp.EVERY_DAY);
        assertEquals(1.0f, DailyUpkeep.day(state(0.5, 1.0f, true, true, true), false, 0.9, deadly, TUNING).damage(), 1e-6);
    }

    @Test
    void unfamiliarAnimalsAreNotHurt() {
        assertEquals(0f, DailyUpkeep.day(state(0.25, 10, true, true, true), false, 0.9, HUNGER, TUNING).damage());
    }

    @Test
    void hurtsOffMeansNoDamage() {
        HungerSettings gentle = new HungerSettings(false, 1.0, 1.0, CatchUp.EVERY_DAY);
        assertEquals(0f, DailyUpkeep.day(state(0.5, 10, true, true, true), false, 0.9, gentle, TUNING).damage());
    }

    @Test
    void aNeglectedCaredAnimalEscapesOnALowRoll() {
        DailyUpkeep.Outcome gone = DailyUpkeep.day(state(0.08, 10, true, true, false), false, 0.05, HUNGER, TUNING);
        assertTrue(gone.escapes());
        assertFalse(gone.cared());
        DailyUpkeep.Outcome stays = DailyUpkeep.day(state(0.08, 10, true, true, false), false, 0.5, HUNGER, TUNING);
        assertFalse(stays.escapes());
        assertTrue(stays.cared());
    }

    @Test
    void wildAnimalsNeverEscape() {
        assertFalse(DailyUpkeep.day(state(0.0, 10, false, true, false), false, 0.0, HUNGER, TUNING).escapes());
    }

    @Test
    void petsAndYoungNeverEscape() {
        assertFalse(DailyUpkeep.day(state(0.08, 10, true, true, true), false, 0.0, HUNGER, TUNING).escapes());
        assertFalse(DailyUpkeep.day(state(0.08, 10, true, false, false), false, 0.0, HUNGER, TUNING).escapes());
    }

    @Test
    void daysToSettleByMode() {
        assertEquals(10, DailyUpkeep.daysToSettle(10, CatchUp.EVERY_DAY));
        assertEquals(1, DailyUpkeep.daysToSettle(10, CatchUp.ONE_DAY));
        assertEquals(0, DailyUpkeep.daysToSettle(10, CatchUp.NONE));
        for (CatchUp mode : CatchUp.values()) {
            assertEquals(0, DailyUpkeep.daysToSettle(0, mode));
            // Last night always counts: the modes are only for longer gaps.
            assertEquals(1, DailyUpkeep.daysToSettle(1, mode));
        }
    }
}
