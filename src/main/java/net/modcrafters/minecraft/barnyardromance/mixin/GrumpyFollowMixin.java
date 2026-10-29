package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// A grumpy pet wanders like an untamed one: it neither follows its owner nor teleports to them (that happens in this goal).
@Mixin(FollowOwnerGoal.class)
public class GrumpyFollowMixin {
    @Shadow
    @Final
    private TamableAnimal tamable;

    @Inject(method = {"canUse", "canContinueToUse"}, at = @At("RETURN"), cancellable = true)
    private void barnyardromance$grumpy(CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ() && FamiliarityCare.grumpy(this.tamable)) {
            callback.setReturnValue(false);
        }
    }
}
