package net.modcrafters.minecraft.barnyardromance.forge.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.modcrafters.minecraft.barnyardromance.forge.pail.MilkBucketFluidHandler;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The mod's goat and sheep milk buckets hold their own milk for other mods' tanks. A milk bucket's fluid capability
// comes from the item itself, ahead of any capability attached to the stack, so it's swapped here. initCapabilities is
// Forge's own method, so its name isn't remapped.
@Mixin(MilkBucketItem.class)
public class MilkBucketItemMixin {
    @Inject(method = "initCapabilities", at = @At("HEAD"), cancellable = true, remap = false)
    private void barnyardromance$ownMilk(ItemStack stack, @Nullable CompoundTag tag, CallbackInfoReturnable<ICapabilityProvider> callback) {
        MilkBucketFluidHandler handler = MilkBucketFluidHandler.forBucket(stack);
        if (handler != null) {
            callback.setReturnValue(handler);
        }
    }
}
