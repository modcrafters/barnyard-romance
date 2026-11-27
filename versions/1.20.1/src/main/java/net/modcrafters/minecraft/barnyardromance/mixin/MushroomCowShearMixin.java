package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.animal.Conversions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

// 1.20.1 and 1.21.1 only: a sheared mooshroom doesn't go through Mob.convertTo there. It discards itself, then adds a
// cow it made by hand, so the record is carried before the discard, or the herd book would see a death (see
// MobConversionMixin). On Forge 1.20.1 both the shears (IForgeShearable.onSheared) and a dispenser (shear) shear
// through Forge's own shearInternal, which does the conversion; Forge adds it, so it has no obfuscated name.
@Mixin(MushroomCow.class)
public class MushroomCowShearMixin {
    @Inject(
            method = "shearInternal",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/MushroomCow;discard()V", remap = true)
    )
    private void barnyardromance$carryRecord(SoundSource source, CallbackInfoReturnable<List<ItemStack>> callback, @Local Cow cow) {
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
