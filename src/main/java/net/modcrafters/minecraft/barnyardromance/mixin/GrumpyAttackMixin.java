package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// A grumpy pet doesn't join in when its owner attacks something.
@Mixin(OwnerHurtTargetGoal.class)
public class GrumpyAttackMixin {
    @Shadow
    @Final
    private TamableAnimal tameAnimal;

    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    private void barnyardromance$grumpy(CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ() && FamiliarityCare.grumpy(this.tameAnimal)) {
            callback.setReturnValue(false);
        }
    }
}
