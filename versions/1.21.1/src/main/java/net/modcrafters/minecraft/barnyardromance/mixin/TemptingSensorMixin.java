package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The 1.21.1 copy of src/.../mixin/TemptingSensorMixin. isTemptation only gets the item here, so doTick remembers
// which mob it is sensing for. Every mob's brain makes its own sensor, so the field never mixes two mobs.
@Mixin(TemptingSensor.class)
public class TemptingSensorMixin {
    @Unique
    private PathfinderMob barnyardromance$mob;

    @Inject(method = "doTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PathfinderMob;)V", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$ignoreFood(ServerLevel level, PathfinderMob mob, CallbackInfo callback) {
        this.barnyardromance$mob = mob;
        if (mob instanceof Animal animal && FamiliarityActions.ignoresFood(animal)) {
            mob.getBrain().eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
            callback.cancel();
        }
    }

    @Inject(method = "isTemptation", at = @At("RETURN"), cancellable = true)
    private void barnyardromance$dietFood(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue() && this.barnyardromance$mob instanceof Animal animal && FamiliarityActions.tempts(animal, stack)) {
            callback.setReturnValue(true);
        }
    }
}
