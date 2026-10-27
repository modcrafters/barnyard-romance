package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarShearing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// versions/1.21.1 has its own copy: NeoForge's IShearable decides there.
@Mixin(ShearsDispenseItemBehavior.class)
public class ShearsDispenseMixin {
    @Inject(method = "tryShearEntity", at = @At("HEAD"), cancellable = true)
    private static void barnyardromance$familiar(ServerLevel level, BlockPos pos, ItemStack tool, CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(FamiliarShearing.tryShear(level, pos, tool));
    }
}
