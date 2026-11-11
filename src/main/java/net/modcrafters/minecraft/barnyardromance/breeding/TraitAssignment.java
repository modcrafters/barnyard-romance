package net.modcrafters.minecraft.barnyardromance.breeding;

import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import java.util.Optional;
import java.util.function.Supplier;

public final class TraitAssignment {
    private TraitAssignment() {
    }

    public static Optional<AnimalTraits> ensure(Optional<AnimalTraits> existing, boolean speciesEnabled, Supplier<AnimalTraits> roll) {
        if (!speciesEnabled) {
            return Optional.empty();
        }
        return Optional.of(existing.orElseGet(roll));
    }

    public static Optional<Double> scaleToApply(AnimalTraits traits, boolean baby) {
        return Optional.of(baby ? 1.0 : traits.scale());
    }
}
