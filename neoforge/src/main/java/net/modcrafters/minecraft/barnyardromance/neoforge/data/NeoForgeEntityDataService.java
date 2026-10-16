package net.modcrafters.minecraft.barnyardromance.neoforge.data;

import net.modcrafters.minecraft.barnyardromance.data.EntityDataService;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public class NeoForgeEntityDataService implements EntityDataService {
    @Override
    public Optional<AnimalTraits> get(Animal animal) {
        return Optional.ofNullable(animal.getExistingDataOrNull(NeoForgeAnimalData.ANIMAL_TRAITS));
    }

    @Override
    public void set(Animal animal, AnimalTraits traits) {
        animal.setData(NeoForgeAnimalData.ANIMAL_TRAITS, traits);
    }

    @Override
    public Optional<Pregnancy> getPregnancy(Animal animal) {
        return Optional.ofNullable(animal.getExistingDataOrNull(NeoForgeAnimalData.PREGNANCY));
    }

    @Override
    public void setPregnancy(Animal animal, Pregnancy pregnancy) {
        animal.setData(NeoForgeAnimalData.PREGNANCY, pregnancy);
    }

    @Override
    public void clearPregnancy(Animal animal) {
        animal.removeData(NeoForgeAnimalData.PREGNANCY);
    }
}
