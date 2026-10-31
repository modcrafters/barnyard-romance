package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.Sulk;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// A grumpy pet is still tamed, so it follows its owner (unless told to sit), but not while it sits sulking (see Sulk).
@Mixin(FollowOwnerGoal.class)
public class GrumpyFollowMixin {
    @Shadow
    @Final
    private TamableAnimal tamable;

    @Inject(method = {"canUse", "canContinueToUse"}, at = @At("RETURN"), cancellable = true)
    private void barnyardromance$grumpy(CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ() && Sulk.sulking(this.tamable)) {
            callback.setReturnValue(false);
        }
    }
}
