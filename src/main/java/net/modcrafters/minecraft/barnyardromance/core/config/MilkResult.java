package net.modcrafters.minecraft.barnyardromance.core.config;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;

import java.util.Optional;

// What a feature makes of a built-in milk entry for one animal, for results an entry's item id can't name (a mod animal
// whose milk depends on its kind). It lives here so core doesn't depend on a feature. Never consulted for a config list.
public interface MilkResult {
    Optional<Item> resultFor(Animal animal, MilkEntry entry);
}
