package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.minecraft.world.entity.Mob;
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
    protected Mob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$ignoreFood(CallbackInfoReturnable<Boolean> callback) {
        if (this.mob instanceof Animal animal && FamiliarityActions.ignoresFood(animal)) {
            callback.setReturnValue(false);
        }
    }
}
