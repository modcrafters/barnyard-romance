package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.modcrafters.minecraft.barnyardromance.shear.ShearActions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The 1.20.1 copy of src/.../mixin/ShearsDispenseMixin, from the 1.21.1 one. Forge's dispenser, like NeoForge's on
// 1.21.1, asks the shearable entity before shearing it (the target below is the loader's patch); an unfamiliar animal,
// or a sheep already sheared today, says no.
@Mixin(ShearsDispenseItemBehavior.class)
public class ShearsDispenseMixin {
    @ModifyExpressionValue(
            method = "tryShearLivingEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/IShearable;isShearable(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private static boolean barnyardromance$familiar(boolean shearable, @Local LivingEntity entity) {
        return shearable && !(entity instanceof Animal animal && (ShearActions.denied(animal) || ShearActions.withholds(animal)));
    }

    // Sheep count themselves in SheepShearMixin; any other tracked animal is counted once NeoForge has sheared it (a
    // mooshroom is already a cow by then).
    @Inject(
            method = "tryShearLivingEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/IShearable;onSheared(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Ljava/util/List;",
                    shift = At.Shift.AFTER
            )
    )
    private static void barnyardromance$countShearing(ServerLevel level, BlockPos pos, ItemStack tool, CallbackInfoReturnable<Boolean> callback, @Local LivingEntity entity) {
        if (entity instanceof Animal animal && !(animal instanceof Sheep)) {
            ShearActions.recordAfterShear(animal);
        }
    }
}
