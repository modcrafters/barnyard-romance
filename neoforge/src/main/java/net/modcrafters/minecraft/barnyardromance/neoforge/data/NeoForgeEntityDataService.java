package net.modcrafters.minecraft.barnyardromance.neoforge.data;

import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.core.data.EntityDataService;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public class NeoForgeEntityDataService implements EntityDataService {
    @Override
    public <T> Optional<T> get(Animal animal, AttachmentKey<T> key) {
        return Optional.ofNullable(animal.getExistingDataOrNull(NeoForgeAnimalData.type(key)));
    }

    @Override
    public <T> void set(Animal animal, AttachmentKey<T> key, T value) {
        animal.setData(NeoForgeAnimalData.type(key), value);
    }

    @Override
    public void remove(Animal animal, AttachmentKey<?> key) {
        animal.removeData(NeoForgeAnimalData.type(key));
    }
}
