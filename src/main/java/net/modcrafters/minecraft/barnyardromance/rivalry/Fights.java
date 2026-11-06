package net.modcrafters.minecraft.barnyardromance.rivalry;

import net.minecraft.world.entity.animal.Animal;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Males fighting each other, by UUID. Fights live only while their animals are loaded, so nothing is saved.
public final class Fights {
    private static final Map<UUID, UUID> OPPONENTS = new ConcurrentHashMap<>();

    private Fights() {
    }

    public static boolean fighting(Animal animal) {
        return OPPONENTS.containsKey(animal.getUUID());
    }
}
