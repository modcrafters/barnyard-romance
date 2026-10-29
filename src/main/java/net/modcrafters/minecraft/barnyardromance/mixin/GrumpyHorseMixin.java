package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Grumpy;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// A grumpy horse (tamed, but no longer familiar) rears up instead of letting anyone on. A rider already up stays up.
@Mixin(AbstractHorse.class)
public class GrumpyHorseMixin {
    @Inject(method = "doPlayerRide", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$refuseRider(Player player, CallbackInfo callback) {
        AbstractHorse horse = (AbstractHorse) (Object) this;
        if (horse.level().isClientSide() || !FamiliarityCare.grumpy(horse)) {
            return;
        }
        horse.makeMad();
        Grumpy.refuse(horse, player, Grumpy.WONT_RIDE);
        callback.cancel();
    }
}
