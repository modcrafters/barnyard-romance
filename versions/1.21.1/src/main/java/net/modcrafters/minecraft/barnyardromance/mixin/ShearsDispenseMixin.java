package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.shear.ShearActions;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// The 1.21.1 copy of src/.../mixin/ShearsDispenseMixin. 1.21.1 ships for NeoForge only, whose dispenser asks
// IShearable before shearing each entity (the target below is NeoForge's patch); an unfamiliar animal, or a sheep
// already sheared today, says no.
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
        return shearable && !(entity instanceof Animal animal && (FamiliarityActions.denies(animal) || ShearActions.shearedToday(animal)));
    }
}
