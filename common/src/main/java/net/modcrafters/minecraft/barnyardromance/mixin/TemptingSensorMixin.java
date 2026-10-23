package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TemptingSensor.class)
public class TemptingSensorMixin {
    @Inject(method = "doTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PathfinderMob;)V", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$ignoreFood(ServerLevel level, PathfinderMob mob, CallbackInfo callback) {
        if (mob instanceof Animal animal && FamiliarityActions.ignoresFood(animal)) {
            mob.getBrain().eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
            callback.cancel();
        }
    }
}
