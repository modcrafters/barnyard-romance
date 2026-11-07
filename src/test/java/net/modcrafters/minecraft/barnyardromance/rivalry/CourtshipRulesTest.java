package net.modcrafters.minecraft.barnyardromance.rivalry;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourtshipRulesTest {
    private static final Predicate<String> ALL = male -> true;

    @Test
    void noMaleMeansNothing() {
        CourtshipRules.Outcome<String> outcome = CourtshipRules.decide(List.of(), ALL, true, rolls(0.0), 1.0);

        assertNull(outcome.mate());
        assertTrue(outcome.fights().isEmpty());
        assertFalse(outcome.rolled());
    }

    @Test
    void aLoneMaleMates() {
        CourtshipRules.Outcome<String> outcome = CourtshipRules.decide(List.of("a"), ALL, true, rolls(0.0), 1.0);

        assertEquals("a", outcome.mate());
        assertTrue(outcome.fights().isEmpty());
        assertFalse(outcome.rolled());
    }

    @Test
    void withoutAFightTheClosestMates() {
        CourtshipRules.Outcome<String> outcome = CourtshipRules.decide(List.of("a", "b", "c"), ALL, true, rolls(0.5), 0.5);

        assertEquals("a", outcome.mate());
        assertTrue(outcome.fights().isEmpty());
        assertTrue(outcome.rolled());
    }

    @Test
    void twoFightAndNobodyMates() {
        CourtshipRules.Outcome<String> outcome = CourtshipRules.decide(List.of("a", "b"), ALL, true, rolls(0.1), 0.5);

        assertNull(outcome.mate());
        assertEquals(List.of(new CourtshipRules.Pair<>("a", "b")), outcome.fights());
    }

    @Test
    void aThirdMatesWhileTwoFight() {
        CourtshipRules.Outcome<String> outcome = CourtshipRules.decide(List.of("a", "b", "c"), ALL, true, rolls(0.1), 0.5);

        assertEquals("c", outcome.mate());
        assertEquals(List.of(new CourtshipRules.Pair<>("a", "b")), outcome.fights());
    }

    @Test
    void fourMalesFightInTwoPairs() {
        CourtshipRules.Outcome<String> outcome = CourtshipRules.decide(List.of("a", "b", "c", "d"), ALL, true, rolls(0.1), 0.5);

        assertNull(outcome.mate());
        assertEquals(List.of(new CourtshipRules.Pair<>("a", "b"), new CourtshipRules.Pair<>("c", "d")), outcome.fights());
    }

    @Test
    void aFifthMatesWhileTwoPairsFight() {
        CourtshipRules.Outcome<String> outcome = CourtshipRules.decide(List.of("a", "b", "c", "d", "e"), ALL, true, rolls(0.1), 0.5);

        assertEquals("e", outcome.mate());
        assertEquals(2, outcome.fights().size());
    }

    // The next pair that doesn't fight ends it: the closer of the two mates, and the rest are left alone.
    @Test
    void aPairThatDoesNotFightEndsIt() {
        CourtshipRules.Outcome<String> outcome = CourtshipRules.decide(List.of("a", "b", "c", "d", "e"), ALL, true, rolls(0.1, 0.9), 0.5);

        assertEquals("c", outcome.mate());
        assertEquals(List.of(new CourtshipRules.Pair<>("a", "b")), outcome.fights());
    }

    @Test
    void aSpeciesThatDoesNotFightTakesTheClosest() {
        CourtshipRules.Outcome<String> outcome = CourtshipRules.decide(List.of("a", "b", "c"), ALL, false, rolls(0.0), 1.0);

        assertEquals("a", outcome.mate());
        assertTrue(outcome.fights().isEmpty());
        assertFalse(outcome.rolled());
    }

    @Test
    void aMaleWhoCannotReachHerIsSkipped() {
        CourtshipRules.Outcome<String> outcome = CourtshipRules.decide(List.of("a", "b", "c"), male -> !male.equals("b"), true, rolls(0.1), 0.5);

        assertNull(outcome.mate());
        assertEquals(List.of(new CourtshipRules.Pair<>("a", "c")), outcome.fights());
    }

    // Reach is a path search, so a male is only looked at once the walk gets to him.
    @Test
    void pathsAreOnlySearchedUntilAMateIsFound() {
        List<String> checked = new ArrayList<>();
        Predicate<String> reachable = male -> checked.add(male);

        CourtshipRules.decide(List.of("a", "b", "c", "d", "e"), reachable, true, rolls(0.9), 0.5);
        assertEquals(List.of("a", "b"), checked);

        checked.clear();
        CourtshipRules.decide(List.of("a", "b", "c", "d", "e"), reachable, false, rolls(0.0), 1.0);
        assertEquals(List.of("a"), checked);
    }

    @Test
    void eachPairRollsOnce() {
        int[] count = {0};
        DoubleSupplier roll = () -> {
            count[0]++;
            return 0.1;
        };

        CourtshipRules.decide(List.of("a", "b", "c", "d", "e"), ALL, true, roll, 0.5);

        assertEquals(2, count[0]);
    }

    private static DoubleSupplier rolls(double... values) {
        int[] next = {0};
        return () -> values[Math.min(next[0]++, values.length - 1)];
    }
}
