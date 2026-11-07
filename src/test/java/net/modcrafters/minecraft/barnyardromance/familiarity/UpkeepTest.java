package net.modcrafters.minecraft.barnyardromance.familiarity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpkeepTest {
    @Test
    void runningAwayClearsCared() {
        Upkeep upkeep = Upkeep.NONE.caredFor().runAway();

        assertFalse(upkeep.cared());
        assertTrue(upkeep.ranAway());
    }

    @Test
    void beingCaredForAgainClearsTheNote() {
        assertFalse(Upkeep.NONE.runAway().caredFor().ranAway());
    }

    @Test
    void fedByRemembersTheDay() {
        Upkeep upkeep = Upkeep.NONE.fedBy("manger", 7L);

        assertTrue(upkeep.fedByFeederOn(7L));
        assertFalse(upkeep.fedByFeederOn(8L));
    }
}
