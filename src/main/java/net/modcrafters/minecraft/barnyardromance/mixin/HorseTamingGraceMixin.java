package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// A horse tamed by riding it starts out a week's hunger above the threshold, or it would refuse its new rider at once
// (grumpy), or after its first missed meal.
@Mixin(AbstractHorse.class)
public class HorseTamingGraceMixin {
    @Inject(method = "tameWithName", at = @At("TAIL"))
    private void barnyardromance$familiar(Player player, CallbackInfoReturnable<Boolean> callback) {
        FamiliarityCare.raiseToTamingGrace((AbstractHorse) (Object) this);
    }
}
