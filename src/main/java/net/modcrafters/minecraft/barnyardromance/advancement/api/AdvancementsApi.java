package net.modcrafters.minecraft.barnyardromance.advancement.api;

import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

// What other features ask the advancements.
public interface AdvancementsApi {
    // The player credited with breeding the pair, if anyone put either of them in love.
    @Nullable UUID breeder(Animal mother, Animal father);
}
