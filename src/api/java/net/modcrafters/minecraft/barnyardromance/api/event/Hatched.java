package net.modcrafters.minecraft.barnyardromance.api.event;

import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A chick hatched. mother and father are known for a fertilized egg, and null for a wild one.
 */
public record Hatched(Animal chick, @Nullable UUID mother, @Nullable UUID father) {
}
