package net.modcrafters.minecraft.barnyardromance.fabric.data;

import net.modcrafters.minecraft.barnyardromance.data.EntityDataService;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public class FabricEntityDataService implements EntityDataService {
    @Override
    public Optional<AnimalTraits> get(Animal animal) {
        return Optional.ofNullable(animal.getAttached(FabricAnimalData.ANIMAL_TRAITS));
    }

    @Override
    public void set(Animal animal, AnimalTraits traits) {
        animal.setAttached(FabricAnimalData.ANIMAL_TRAITS, traits);
    }

    @Override
    public Optional<Pregnancy> getPregnancy(Animal animal) {
        return Optional.ofNullable(animal.getAttached(FabricAnimalData.PREGNANCY));
    }

    @Override
    public void setPregnancy(Animal animal, Pregnancy pregnancy) {
        animal.setAttached(FabricAnimalData.PREGNANCY, pregnancy);
    }

    @Override
    public void clearPregnancy(Animal animal) {
        animal.removeAttached(FabricAnimalData.PREGNANCY);
    }
}
