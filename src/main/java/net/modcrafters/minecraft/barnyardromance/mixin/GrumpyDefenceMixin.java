package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Sulk;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// When its owner is attacked, a grumpy pet sits down and watches instead of defending them (see Sulk). The goal
// never starts, so the hit is marked as seen here the way its start() would, or every check would count it again.
@Mixin(OwnerHurtByTargetGoal.class)
public class GrumpyDefenceMixin {
    @Shadow
    @Final
    private TamableAnimal tameAnimal;
    @Shadow
    private int timestamp;

    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    private void barnyardromance$sulk(CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ() && FamiliarityCare.grumpy(this.tameAnimal)) {
            LivingEntity owner = this.tameAnimal.getOwner();
            if (owner != null) {
                this.timestamp = owner.getLastHurtByMobTimestamp();
                Sulk.ownerHurt(this.tameAnimal, owner, this.tameAnimal.level().getGameTime());
            }
            callback.setReturnValue(false);
        }
    }
}
