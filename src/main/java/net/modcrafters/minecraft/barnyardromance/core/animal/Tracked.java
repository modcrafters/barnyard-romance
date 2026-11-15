package net.modcrafters.minecraft.barnyardromance.core.animal;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;

import java.util.Optional;

// The animals the mod looks after: their species is switched on in the config, and they have traits. An animal whose
// species was switched off or excluded keeps the traits it had, and the mod leaves them alone.
public final class Tracked {
    private Tracked() {
    }

    // The animal's traits, if its species is switched on.
    public static Optional<AnimalTraits> traits(Animal animal) {
        if (!BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return Optional.empty();
        }
        return Services.DATA.get(animal);
    }
}
