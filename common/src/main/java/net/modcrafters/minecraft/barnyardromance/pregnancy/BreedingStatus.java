package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;
import java.util.function.UnaryOperator;

// Mirrors love mode and the breeding rest into the synced traits so the inspector can show them.
public final class BreedingStatus {
    private static boolean warned;

    private BreedingStatus() {
    }

    public static void inLove(Animal animal) {
        update(animal, traits -> {
            long until = animal.level().getGameTime() + animal.getInLoveTime();
            return traits.loveUntil() == until ? traits : traits.withLove(until);
        });
    }

    public static void outOfLove(Animal animal) {
        update(animal, traits -> traits.loveUntil() < 0L ? traits : traits.withLove(-1L));
    }

    // Called after vanilla has set the parent's age to its breeding cooldown.
    public static void resting(Animal animal) {
        update(animal, traits -> traits.withLove(-1L).withCooldown(animal.level().getGameTime() + Math.max(0, animal.getAge())));
    }

    private static void update(Animal animal, UnaryOperator<AnimalTraits> change) {
        try {
            if (animal.level() == null || animal.level().isClientSide()) {
                return;
            }
            Optional<AnimalTraits> traits = Services.DATA.get(animal);
            if (traits.isEmpty()) {
                return;
            }
            AnimalTraits next = change.apply(traits.get());
            if (!next.equals(traits.get())) {
                Services.DATA.set(animal, next);
            }
        } catch (RuntimeException exception) {
            if (!warned) {
                warned = true;
                Constants.LOG.error("Failed to record love or rest, the inspector may be out of date", exception);
            }
        }
    }
}
