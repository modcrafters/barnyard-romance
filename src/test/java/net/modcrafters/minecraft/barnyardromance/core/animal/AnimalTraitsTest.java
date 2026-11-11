package net.modcrafters.minecraft.barnyardromance.core.animal;

import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.familiarity.Origin;
import net.modcrafters.minecraft.barnyardromance.familiarity.Refusals;
import net.modcrafters.minecraft.barnyardromance.familiarity.Upkeep;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.modcrafters.minecraft.barnyardromance.vigour.Vigour;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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

    @Test
    void oldTraitsHaveUnknownParents() {
        assertEquals(Lineage.UNKNOWN, new AnimalTraits(Sex.FEMALE, 0.9).lineage());
    }

    @Test
    void withLineageKeepsEveryOtherTrait() {
        AnimalTraits traits = new AnimalTraits(Sex.FEMALE, 0.9).withWeight(0.8).withFertility(0.5, 100L).withBornTick(30L).fed(0.4, 7L);
        Lineage lineage = new Lineage(UUID.fromString("00000000-0000-0000-0000-000000000001"), null);

        AnimalTraits withParents = traits.withLineage(lineage);

        assertEquals(lineage, withParents.lineage());
        assertEquals(traits, withParents.withLineage(Lineage.UNKNOWN));
        assertEquals(lineage, withParents.withWeight(0.1).withFertility(0.2, 9L).withBornTick(1L).fed(0.9, 2L)
                .withLove(3L).withCooldown(4L).gaveBirth(5L, "minecraft:cow").lineage());
    }

    @Test
    void oldTraitsHaveNoHunger() {
        assertEquals(Hunger.NONE, new AnimalTraits(Sex.FEMALE, 0.9).hunger());
    }

    @Test
    void copiesKeepHunger() {
        Hunger hunger = new Hunger(10L, 9L, 8L);
        AnimalTraits traits = new AnimalTraits(Sex.FEMALE, 0.9).withHunger(hunger);

        AnimalTraits copied = traits.fed(0.4, 7L).gaveBirth(70L, "minecraft:cow").withLove(50L).withCooldown(60L).withWeight(0.8)
                .withFertility(0.5, 100L).withBornTick(30L).withLactation(traits.lactation()).sheared(5L).withLineage(Lineage.UNKNOWN);

        assertEquals(hunger, copied.hunger());
    }

    // The codec round trip needs the game's serialization classes, so it is the old_traits_load_without_upkeep game test.
    @Test
    void upkeepIsKeptByEveryWither() {
        Upkeep upkeep = new Upkeep(true, false, 12L, "manger", true);
        AnimalTraits traits = new AnimalTraits(Sex.FEMALE, 0.9).withUpkeep(upkeep).withWeight(0.8).fed(0.4, 7L).withLove(50L).withCooldown(60L)
                .gaveBirth(70L, "minecraft:cow").withFertility(0.5, 9L).withBornTick(3L).withLactation(Lactation.NONE).sheared(5L).withLineage(Lineage.UNKNOWN)
                .withHunger(Hunger.NONE).withOrigin(Origin.WILD).withVigour(Vigour.NONE).withRefusals(Refusals.NONE);

        assertEquals(upkeep, traits.upkeep());
        assertEquals(Upkeep.NONE, new AnimalTraits(Sex.FEMALE, 0.9).upkeep());
    }
}
