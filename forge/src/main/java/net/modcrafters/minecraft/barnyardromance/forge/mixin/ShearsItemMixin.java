package net.modcrafters.minecraft.barnyardromance.forge.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ShearsItem;
import net.modcrafters.minecraft.barnyardromance.shear.ShearActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Forge moves player shearing out of the animal into ShearsItem, which runs even after the animal refused
// (FamiliarityActions.refusesProduct, ShearActions.refuses), so the same IForgeShearable check is refused here for an
// unfamiliar animal, or a sheep already sheared today. The counterpart of NeoForge's ShearsItemMixin.
@Mixin(ShearsItem.class)
public class ShearsItemMixin {
    @ModifyExpressionValue(
            method = "interactLivingEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/IForgeShearable;isShearable(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z",
                    remap = false
            )
    )
    private boolean barnyardromance$familiar(boolean shearable, @Local(argsOnly = true) LivingEntity entity) {
        return shearable && !(entity instanceof Animal animal && (ShearActions.denied(animal) || ShearActions.blocked(animal)));
    }
}
