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
    void oldTraitsHaveNoBirthTick() {
        assertEquals(-1L, new AnimalTraits(Sex.FEMALE, 0.9).bornTick());
    }

    @Test
    void withersKeepEveryOtherField() {
        AnimalTraits traits = new AnimalTraits(Sex.FEMALE, 0.9).withWeight(0.8).withFertility(0.5, 100L).withBornTick(30L).fed(0.4, 7L).withLove(50L).withCooldown(60L).gaveBirth(70L, "minecraft:cow");

        assertEquals(0.8, traits.weight(), 1.0e-9);
        assertEquals(0.5, traits.fertility(), 1.0e-9);
        assertEquals(100L, traits.fertilityTick());
        assertEquals(0.4, traits.familiarity(), 1.0e-9);
        assertEquals(50L, traits.loveUntil());
        assertEquals(30L, traits.bornTick());
        assertEquals(30L, traits.withWeight(0.1).withFertility(0.2, 9L).bornTick());
    }
}
