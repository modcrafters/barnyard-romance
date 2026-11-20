package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.animal.Conversions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 1.21.1 only: a sheared mooshroom doesn't go through Mob.convertTo there. It discards itself, then adds a cow it made
// by hand, so the record is carried before the discard, or the herd book would see a death (see MobConversionMixin).
@Mixin(MushroomCow.class)
public class MushroomCowShearMixin {
    @Inject(
            method = "shear(Lnet/minecraft/sounds/SoundSource;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/MushroomCow;discard()V")
    )
    private void barnyardromance$carryRecord(SoundSource source, CallbackInfo callback, @Local Cow cow) {
        MushroomCow mooshroom = (MushroomCow) (Object) this;
        if (mooshroom.level().isClientSide()) {
            return;
        }
        try {
            Conversions.carry(mooshroom, cow);
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to carry a mooshroom's record to the cow it was sheared into", exception);
        }
    }
}
