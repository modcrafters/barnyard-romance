package net.modcrafters.minecraft.barnyardromance.vigour;

import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import org.junit.jupiter.api.Test;

import static net.modcrafters.minecraft.barnyardromance.vigour.VigourSlices.VIGOUR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

// FamiliarityCare settles every animal every tick, and saves nothing when settle hands back the traits it was given.
class VigourApiImplTest {
    private final VigourApiImpl vigour = new VigourApiImpl();

    @Test
    void settlingADayAlreadySettledHandsBackTheSameTraits() {
        AnimalTraits traits = male(new Vigour(0.4, 6L, -1L));

        assertSame(traits, vigour.settle(traits, 5L, 6L));
    }

    @Test
    void settlingANewDayAfterAMealRefillsHim() {
        AnimalTraits traits = male(new Vigour(0.1, 5L, -1L));

        AnimalTraits settled = vigour.settle(traits, 5L, 6L);

        assertNotSame(traits, settled);
        assertEquals(new Vigour(1.0, 6L, -1L), settled.get(VIGOUR));
    }

    @Test
    void aRivalRollIsKeptBesideHisVigour() {
        AnimalTraits rolled = vigour.rolledForRivals(male(new Vigour(0.4, 6L, -1L)), 6L);

        assertEquals(6L, vigour.lastRivalRoll(rolled));
        assertEquals(new Vigour(0.4, 6L, 6L), rolled.get(VIGOUR));
    }

    private static AnimalTraits male(Vigour value) {
        return AnimalTraits.of(Sex.MALE, 1.0).with(VIGOUR, value);
    }
}
