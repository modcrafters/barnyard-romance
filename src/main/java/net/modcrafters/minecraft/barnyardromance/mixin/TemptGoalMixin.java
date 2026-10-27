package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
//? if <1.21.5
//import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TemptGoal.class)
public class TemptGoalMixin {
    @Shadow
    //? if >=1.21.5 {
    protected Mob mob;
    //?} else
    //protected PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$ignoreFood(CallbackInfoReturnable<Boolean> callback) {
        if (this.mob instanceof Animal animal && FamiliarityActions.ignoresFood(animal)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "shouldFollow", at = @At("RETURN"), cancellable = true)
    private void barnyardromance$dietFood(LivingEntity entity, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue() && this.mob instanceof Animal animal
                && (FamiliarityActions.tempts(animal, entity.getMainHandItem()) || FamiliarityActions.tempts(animal, entity.getOffhandItem()))) {
            callback.setReturnValue(true);
        }
    }
}
