package net.modcrafters.minecraft.barnyardromance.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

// One step a feature runs on an animal in an AnimalPhase, registered by name (see FeatureRegistrar.hook).
@FunctionalInterface
public interface AnimalHook {
    void run(Animal animal, ServerLevel level);
}
