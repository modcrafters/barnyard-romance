package net.modcrafters.minecraft.barnyardromance.api;

import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

/**
 * Questions about one animal. Exact on the server; on the client, only what Barnyard Romance syncs to it.
 */
public interface BarnyardAnimals {
    // Barnyard Romance looks after this animal: its species is enabled and it has a sex and size.
    boolean tracked(Animal animal);

    // The animal as the stat card shows it, or empty when it isn't tracked.
    Optional<AnimalInfo> info(Animal animal);
}
