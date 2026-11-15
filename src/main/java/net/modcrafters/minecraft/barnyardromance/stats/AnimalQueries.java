package net.modcrafters.minecraft.barnyardromance.stats;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.api.AnimalInfo;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardAnimals;
import net.modcrafters.minecraft.barnyardromance.api.PregnancyInfo;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClasses;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeRanges;
import net.modcrafters.minecraft.barnyardromance.core.animal.Tracked;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyApi;

import java.util.Optional;

// The public API's questions about an animal, answered from what its stat card reads.
final class AnimalQueries implements BarnyardAnimals {
    @Override
    public boolean tracked(Animal animal) {
        return Tracked.traits(animal).isPresent();
    }

    @Override
    public Optional<AnimalInfo> info(Animal animal) {
        return Tracked.traits(animal).map(traits -> info(animal, traits));
    }

    private static AnimalInfo info(Animal animal, AnimalTraits traits) {
        long now = animal.level().getGameTime();
        FamiliarityApi familiarity = FeaturesRepository.get(FamiliarityApi.class);
        PregnancyApi pregnancies = FeaturesRepository.get(PregnancyApi.class);
        Optional<PregnancyInfo> pregnancy = pregnancies.pregnancy(animal).map(carried -> new PregnancyInfo(
                pregnancies.ticksUntilDue(carried, now), pregnancies.progress(carried, now), carried.fatherSpecies()));
        return new AnimalInfo(
                traits.sex(),
                traits.scale(),
                SizeClasses.of(traits.sex(), traits.scale(), SizeRanges.rangeOf(traits, AnimalSex.speciesId(animal))),
                AgeController.stage(animal, traits),
                traits.bornTick() < 0L ? -1L : Math.max(0L, now - traits.bornTick()),
                familiarity.familiarity(traits),
                familiarity.isFamiliar(traits),
                familiarity.wildCap(animal, traits) < 1.0,
                pregnancy
        );
    }
}
