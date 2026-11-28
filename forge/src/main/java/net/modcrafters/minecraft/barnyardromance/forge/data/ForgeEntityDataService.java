package net.modcrafters.minecraft.barnyardromance.forge.data;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.core.data.EntityDataService;

import java.util.Objects;
import java.util.Optional;

// Animal data in the ForgeAnimalData capability. A change is sent to the players tracking the animal straight away, as
// NeoForge does for a synced attachment that's set or removed.
public class ForgeEntityDataService implements EntityDataService {
    @Override
    public <T> Optional<T> get(Animal animal, AttachmentKey<T> key) {
        ForgeAnimalData.requireRegistered(key);
        ForgeAnimalData.Values values = ForgeAnimalData.of(animal);
        return values == null ? Optional.empty() : Optional.ofNullable(values.get(key));
    }

    @Override
    public <T> void set(Animal animal, AttachmentKey<T> key, T value) {
        Objects.requireNonNull(value);
        ForgeAnimalData.Values values = values(animal, key);
        values.set(key, value);
        AnimalDataSync.changed(animal, key);
    }

    @Override
    public void remove(Animal animal, AttachmentKey<?> key) {
        ForgeAnimalData.Values values = values(animal, key);
        values.remove(key);
        AnimalDataSync.changed(animal, key);
    }

    private static ForgeAnimalData.Values values(Animal animal, AttachmentKey<?> key) {
        ForgeAnimalData.requireRegistered(key);
        ForgeAnimalData.Values values = ForgeAnimalData.of(animal);
        if (values == null) {
            throw new IllegalStateException("No " + ForgeAnimalData.ID + " capability on " + animal + " for " + key.id());
        }
        return values;
    }
}
