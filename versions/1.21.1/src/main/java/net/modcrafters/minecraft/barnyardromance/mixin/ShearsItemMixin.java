package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ShearsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// 1.21.1 only (extra_mixins in versions/1.21.1/gradle.properties). NeoForge moves player shearing out of the
// animal into ShearsItem, which runs even after the animal refused (FamiliarityActions.refusesProduct), so the
// same IShearable check is refused here for an unfamiliar animal.
@Mixin(ShearsItem.class)
public class ShearsItemMixin {
    @ModifyExpressionValue(
            method = "interactLivingEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/IShearable;isShearable(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private boolean barnyardromance$familiar(boolean shearable, @Local(argsOnly = true) LivingEntity entity) {
        return shearable && !(entity instanceof Animal animal && FamiliarityActions.denies(animal));
    }
}
