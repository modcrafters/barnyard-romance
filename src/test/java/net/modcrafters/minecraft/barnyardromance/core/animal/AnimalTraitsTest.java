package net.modcrafters.minecraft.barnyardromance.core.animal;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.familiarity.Origin;
import net.modcrafters.minecraft.barnyardromance.familiarity.Refusals;
import net.modcrafters.minecraft.barnyardromance.familiarity.Upkeep;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.modcrafters.minecraft.barnyardromance.vigour.Vigour;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;
import net.modcrafters.minecraft.barnyardromance.milk.MilkSlices;
import net.modcrafters.minecraft.barnyardromance.pregnancy.BreedingState;
import net.modcrafters.minecraft.barnyardromance.shear.ShearSlices;
import net.modcrafters.minecraft.barnyardromance.vigour.VigourSlices;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;
import static net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancySlices.BREEDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AnimalTraitsTest {
    private static final AnimalSlice<Integer> COUNT = new AnimalSlice<>("count", 7, Codec.INT.optionalFieldOf("count", 7), ByteBufCodecs.VAR_INT);

    @Test
    void unsetSliceReadsAsItsDefault() {
        AnimalTraits traits = AnimalTraits.of(Sex.FEMALE, 0.9);

        assertEquals(7, traits.get(COUNT));
        assertEquals(FamiliarityState.NONE, traits.get(CARE));
    }

    @Test
    void withReturnsACopyAndLeavesTheOriginal() {
        AnimalTraits traits = AnimalTraits.of(Sex.FEMALE, 0.9).withWeight(0.8);

        AnimalTraits counted = traits.with(COUNT, 3);

        assertEquals(3, counted.get(COUNT));
        assertEquals(7, traits.get(COUNT));
        assertEquals(0.8, counted.weight(), 1.0e-9);
        assertNotEquals(traits, counted);
    }

    // A decoded record holds every slice, a new one none; both read the same, so they must be equal.
    @Test
    void aSliceSetToItsDefaultEqualsAnUnsetOne() {
        AnimalTraits traits = AnimalTraits.of(Sex.FEMALE, 0.9);

        assertEquals(traits, traits.with(COUNT, 7));
        assertEquals(traits.hashCode(), traits.with(COUNT, 7).hashCode());
        assertEquals(traits, traits.with(COUNT, 3).with(COUNT, 7));
    }

    @Test
    void oldTraitsDefaultToAverageWeightAndFullFertility() {
        AnimalTraits traits = AnimalTraits.of(Sex.FEMALE, 0.9);

        assertEquals(0.5, traits.weight(), 1.0e-9);
        assertEquals(1.0, traits.get(BREEDING).fertility(), 1.0e-9);
        assertEquals(-1L, traits.get(BREEDING).fertilityTick());
    }

    @Test
    void oldTraitsHaveNoBirthTick() {
        assertEquals(-1L, AnimalTraits.of(Sex.FEMALE, 0.9).bornTick());
    }

    @Test
    void withersKeepEveryOtherField() {
        AnimalTraits traits = AnimalTraits.of(Sex.FEMALE, 0.9).withWeight(0.8).withBornTick(30L)
                .with(BREEDING, BreedingState.NONE.withFertility(0.5, 100L).withLove(50L).withCooldown(60L).gaveBirth(70L, "minecraft:cow"))
                .with(CARE, FamiliarityState.NONE.fed(0.4, 7L));

        assertEquals(0.8, traits.weight(), 1.0e-9);
        assertEquals(0.5, traits.get(BREEDING).fertility(), 1.0e-9);
        assertEquals(100L, traits.get(BREEDING).fertilityTick());
        assertEquals(0.4, traits.get(CARE).familiarity(), 1.0e-9);
        assertEquals(50L, traits.get(BREEDING).loveUntil());
        assertEquals(30L, traits.bornTick());
        assertEquals(30L, traits.withWeight(0.1).with(BREEDING, traits.get(BREEDING).withFertility(0.2, 9L)).bornTick());
    }

    @Test
    void oldTraitsHaveUnknownParents() {
        assertEquals(Lineage.UNKNOWN, AnimalTraits.of(Sex.FEMALE, 0.9).lineage());
    }

    @Test
    void withLineageKeepsEveryOtherTrait() {
        AnimalTraits traits = AnimalTraits.of(Sex.FEMALE, 0.9).withWeight(0.8).withBornTick(30L)
                .with(BREEDING, BreedingState.NONE.withFertility(0.5, 100L)).with(CARE, FamiliarityState.NONE.fed(0.4, 7L));
        Lineage lineage = new Lineage(UUID.fromString("00000000-0000-0000-0000-000000000001"), null);

        AnimalTraits withParents = traits.withLineage(lineage);

        assertEquals(lineage, withParents.lineage());
        assertEquals(traits, withParents.withLineage(Lineage.UNKNOWN));
        assertEquals(lineage, withParents.withWeight(0.1).withBornTick(1L)
                .with(BREEDING, BreedingState.NONE.withFertility(0.2, 9L).withLove(3L).withCooldown(4L).gaveBirth(5L, "minecraft:cow"))
                .with(CARE, FamiliarityState.NONE.fed(0.9, 2L)).lineage());
    }

    @Test
    void oldTraitsHaveNoHunger() {
        assertEquals(Hunger.NONE, AnimalTraits.of(Sex.FEMALE, 0.9).get(CARE).hunger());
    }

    @Test
    void copiesKeepHunger() {
        Hunger hunger = new Hunger(10L, 9L, 8L);
        AnimalTraits traits = AnimalTraits.of(Sex.FEMALE, 0.9).with(CARE, FamiliarityState.NONE.withHunger(hunger));

        AnimalTraits copied = traits.with(CARE, traits.get(CARE).fed(0.4, 7L))
                .with(BREEDING, BreedingState.NONE.gaveBirth(70L, "minecraft:cow").withLove(50L).withCooldown(60L).withFertility(0.5, 100L))
                .withWeight(0.8).withBornTick(30L).with(MilkSlices.LACTATION, traits.get(MilkSlices.LACTATION))
                .with(ShearSlices.LAST_SHEARED, 5L).withLineage(Lineage.UNKNOWN);

        assertEquals(hunger, copied.get(CARE).hunger());
    }

    // The codec round trip is in SaveFixturesTest and the old_traits_load_without_upkeep game test.
    @Test
    void upkeepIsKeptByEveryWither() {
        Upkeep upkeep = new Upkeep(true, false, 12L, "manger", true);
        AnimalTraits traits = AnimalTraits.of(Sex.FEMALE, 0.9).with(CARE, FamiliarityState.NONE.withUpkeep(upkeep).fed(0.4, 7L)
                        .withHunger(Hunger.NONE).withOrigin(Origin.WILD).withRefusals(Refusals.NONE))
                .withWeight(0.8)
                .with(BREEDING, BreedingState.NONE.withLove(50L).withCooldown(60L).gaveBirth(70L, "minecraft:cow").withFertility(0.5, 9L))
                .withBornTick(3L).with(MilkSlices.LACTATION, Lactation.NONE).with(ShearSlices.LAST_SHEARED, 5L).withLineage(Lineage.UNKNOWN)
                .with(VigourSlices.VIGOUR, Vigour.NONE);

        assertEquals(upkeep, traits.get(FamiliaritySlices.CARE).upkeep());
        assertEquals(Upkeep.NONE, AnimalTraits.of(Sex.FEMALE, 0.9).get(CARE).upkeep());
    }
}
