package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Taming sets a wolf's health to vanilla's 40; a big wolf has more than that (see WolfStrength), so it starts full.
// A tamed wolf loading from its save skips this, so its health is not reset.
@Mixin(Wolf.class)
public class WolfTamingMixin {
    @Inject(method = "applyTamingSideEffects", at = @At("TAIL"))
    private void barnyardromance$fullHealth(CallbackInfo callback) {
        Wolf wolf = (Wolf) (Object) this;
        if (wolf.isTame()) {
            wolf.setHealth(wolf.getMaxHealth());
        }
    }
}
