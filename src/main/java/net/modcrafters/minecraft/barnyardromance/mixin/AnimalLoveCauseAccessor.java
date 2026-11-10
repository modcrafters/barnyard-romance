package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// The player who put an animal in love, kept even while they're offline or in another dimension; vanilla's
// getLoveCause only finds them when they're in the animal's level. versions/1.21.1 has its own copy.
@Mixin(Animal.class)
public interface AnimalLoveCauseAccessor {
    @Accessor("loveCause")
    @Nullable EntityReference<ServerPlayer> barnyardromance$loveCause();
}
