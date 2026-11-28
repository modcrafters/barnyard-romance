package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// 1.20.1 only: every entity has its own random, but only living ones have a getter for it (see compat.legacy.Legacy).
@Mixin(Entity.class)
public interface EntityRandomAccessor {
    @Accessor("random")
    RandomSource barnyardromance$random();
}
