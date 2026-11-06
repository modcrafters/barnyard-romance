package net.modcrafters.minecraft.barnyardromance.rivalry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CourtshipRulesTest {
    @Test
    void noMaleMeansNothing() {
        assertEquals(new CourtshipRules.Outcome(-1, -1, -1), CourtshipRules.decide(0, true, 0.0, 1.0));
    }

    @Test
    void oneMaleMatesWithHer() {
        assertEquals(new CourtshipRules.Outcome(0, -1, -1), CourtshipRules.decide(1, true, 0.0, 1.0));
    }

    @Test
    void aLuckyRollMeansNoFight() {
        assertEquals(new CourtshipRules.Outcome(0, -1, -1), CourtshipRules.decide(3, true, 0.5, 0.5));
    }

    @Test
    void twoMalesFightAndNobodyMatesYet() {
        assertEquals(new CourtshipRules.Outcome(-1, 0, 1), CourtshipRules.decide(2, true, 0.1, 0.5));
    }

    @Test
    void aThirdMaleMatesWhileTheOtherTwoFight() {
        assertEquals(new CourtshipRules.Outcome(2, 0, 1), CourtshipRules.decide(3, true, 0.1, 0.5));
        assertEquals(new CourtshipRules.Outcome(2, 0, 1), CourtshipRules.decide(5, true, 0.1, 0.5));
    }

    @Test
    void speciesThatDontFightTakeTheClosest() {
        assertEquals(new CourtshipRules.Outcome(0, -1, -1), CourtshipRules.decide(5, false, 0.0, 1.0));
    }

    @Test
    void chanceZeroNeverFights() {
        assertEquals(new CourtshipRules.Outcome(0, -1, -1), CourtshipRules.decide(2, true, 0.0, 0.0));
    }
}
