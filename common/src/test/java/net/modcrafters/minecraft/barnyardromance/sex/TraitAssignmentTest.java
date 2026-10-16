package net.modcrafters.minecraft.barnyardromance.sex;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraitAssignmentTest {
    @Test
    void missingTraitsAreRolledOnceWhenTheSpeciesIsEnabled() {
        Optional<AnimalTraits> assigned = TraitAssignment.ensure(
                Optional.empty(),
                true,
                () -> new AnimalTraits(Sex.FEMALE, 0.93)
        );

        assertEquals(Optional.of(new AnimalTraits(Sex.FEMALE, 0.93)), assigned);
    }

    @Test
    void existingTraitsAreKeptAndTheRollIsNotConsumed() {
        AnimalTraits existing = new AnimalTraits(Sex.MALE, 1.2);
        AtomicInteger rolls = new AtomicInteger();

        Optional<AnimalTraits> assigned = TraitAssignment.ensure(
                Optional.of(existing),
                true,
                () -> {
                    rolls.incrementAndGet();
                    return new AnimalTraits(Sex.FEMALE, 0.5);
                }
        );

        assertEquals(Optional.of(existing), assigned);
        assertEquals(0, rolls.get());
    }

    @Test
    void aDisabledSpeciesIsLeftUntouched() {
        AtomicInteger rolls = new AtomicInteger();

        Optional<AnimalTraits> assigned = TraitAssignment.ensure(
                Optional.empty(),
                false,
                () -> {
                    rolls.incrementAndGet();
                    return new AnimalTraits(Sex.FEMALE, 0.9);
                }
        );

        assertTrue(assigned.isEmpty());
        assertEquals(0, rolls.get());
    }

    @Test
    void babiesUseVanillaScaleUntilTheyGrow() {
        AnimalTraits traits = new AnimalTraits(Sex.FEMALE, 0.93);
        assertEquals(Optional.of(1.0), TraitAssignment.scaleToApply(traits, true));
        assertEquals(Optional.of(0.93), TraitAssignment.scaleToApply(traits, false));
    }
}
