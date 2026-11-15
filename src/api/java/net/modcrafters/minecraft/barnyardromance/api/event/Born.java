package net.modcrafters.minecraft.barnyardromance.api.event;

import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * A mother gave birth. breeder is the player who bred her, when known.
 */
public record Born(Animal mother, List<Animal> young, @Nullable UUID breeder) {
}
