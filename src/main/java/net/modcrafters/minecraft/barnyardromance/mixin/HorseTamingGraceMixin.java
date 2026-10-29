package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// A horse tamed by riding it starts out at least familiar, or it would refuse its new rider at once (grumpy).
@Mixin(AbstractHorse.class)
public class HorseTamingGraceMixin {
    @Inject(method = "tameWithName", at = @At("TAIL"))
    private void barnyardromance$familiar(Player player, CallbackInfoReturnable<Boolean> callback) {
        FamiliarityCare.raiseToThreshold((AbstractHorse) (Object) this);
    }
}
