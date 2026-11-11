package net.modcrafters.minecraft.barnyardromance.core.data;

import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public interface EntityDataService {
    <T> Optional<T> get(Animal animal, AttachmentKey<T> key);

    <T> void set(Animal animal, AttachmentKey<T> key, T value);

    void remove(Animal animal, AttachmentKey<?> key);

    default Optional<AnimalTraits> get(Animal animal) {
        return get(animal, CoreAttachments.ANIMAL_TRAITS);
    }

    default void set(Animal animal, AnimalTraits traits) {
        set(animal, CoreAttachments.ANIMAL_TRAITS, traits);
    }
}
