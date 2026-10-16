package net.modcrafters.minecraft.barnyardromance.data;

import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public interface EntityDataService {
    Optional<AnimalTraits> get(Animal animal);

    void set(Animal animal, AnimalTraits traits);

    Optional<Pregnancy> getPregnancy(Animal animal);

    void setPregnancy(Animal animal, Pregnancy pregnancy);

    void clearPregnancy(Animal animal);
}
