package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.api.event.FamiliarityChange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// What a familiarity change comes to once other mods have seen it. The events are built without an animal: outcome
// only reads what the listeners left on them.
class FamiliarityChangesTest {
    private static FamiliarityChange change(double before, double after) {
        return new FamiliarityChange(null, before, after, FamiliarityChange.Reason.CARE);
    }

    @Test
    void anUntouchedChangeIsKept() {
        assertEquals(0.6, FamiliarityChanges.outcome(change(0.5, 0.6), 0.6, 1.0));
    }

    // A wild-caught animal already above its cap from before the cap existed keeps its value when nobody changes it.
    @Test
    void anUntouchedChangeAboveTheCapIsKept() {
        assertEquals(0.85, FamiliarityChanges.outcome(change(0.9, 0.85), 0.85, 0.4));
    }

    @Test
    void aboveTheCapIsClamped() {
        FamiliarityChange change = change(0.3, 0.35);
        change.setAfter(0.9);

        assertEquals(0.4, FamiliarityChanges.outcome(change, 0.35, 0.4));
    }

    // An animal above its cap (tamed before caps, or the cap was lowered) can be held or slowed by a listener, but
    // never pushed higher than it already is.
    @Test
    void aListenerCanSlowTheFallOfAnAnimalAboveItsCap() {
        FamiliarityChange change = change(0.75, 0.73);
        change.setAfter(0.74);

        assertEquals(0.74, FamiliarityChanges.outcome(change, 0.73, 0.4));
    }

    @Test
    void aListenerCannotRaiseAnAnimalAboveItsCapFurther() {
        FamiliarityChange change = change(0.75, 0.73);
        change.setAfter(0.9);

        assertEquals(0.75, FamiliarityChanges.outcome(change, 0.73, 0.4));
    }

    @Test
    void negativeIsClampedToZero() {
        FamiliarityChange change = change(0.3, 0.25);
        change.setAfter(-2.0);

        assertEquals(0.0, FamiliarityChanges.outcome(change, 0.25, 1.0));
    }

    @Test
    void nanKeepsBefore() {
        FamiliarityChange change = change(0.3, 0.35);
        change.setAfter(Double.NaN);

        assertEquals(0.3, FamiliarityChanges.outcome(change, 0.35, 1.0));
    }

    @Test
    void cancelKeepsBefore() {
        FamiliarityChange change = change(0.3, 0.35);
        change.setAfter(0.5);
        change.cancel();

        assertEquals(0.3, FamiliarityChanges.outcome(change, 0.35, 1.0));
    }
}
