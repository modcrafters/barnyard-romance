package net.modcrafters.minecraft.barnyardromance.sex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnimalTraitsTest {
    @Test
    void oldTraitsDefaultToAverageWeightAndFullFertility() {
        AnimalTraits traits = new AnimalTraits(Sex.FEMALE, 0.9);

        assertEquals(0.5, traits.weight(), 1.0e-9);
        assertEquals(1.0, traits.fertility(), 1.0e-9);
        assertEquals(-1L, traits.fertilityTick());
    }

    @Test
    void withersKeepEveryOtherField() {
        AnimalTraits traits = new AnimalTraits(Sex.FEMALE, 0.9).withWeight(0.8).withFertility(0.5, 100L).fed(0.4, 7L).withLove(50L);

        assertEquals(0.8, traits.weight(), 1.0e-9);
        assertEquals(0.5, traits.fertility(), 1.0e-9);
        assertEquals(100L, traits.fertilityTick());
        assertEquals(0.4, traits.familiarity(), 1.0e-9);
        assertEquals(50L, traits.loveUntil());
    }
}
