package net.modcrafters.minecraft.barnyardromance.fabric.data;

import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.core.data.EntityDataService;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public class FabricEntityDataService implements EntityDataService {
    @Override
    public <T> Optional<T> get(Animal animal, AttachmentKey<T> key) {
        return Optional.ofNullable(animal.getAttached(FabricAnimalData.type(key)));
    }

    @Override
    public <T> void set(Animal animal, AttachmentKey<T> key, T value) {
        animal.setAttached(FabricAnimalData.type(key), value);
    }

    @Override
    public void remove(Animal animal, AttachmentKey<?> key) {
        animal.removeAttached(FabricAnimalData.type(key));
    }
}
