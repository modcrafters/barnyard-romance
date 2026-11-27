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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The 1.20.1 copy of src/.../mixin/ShearsDispenseMixin, from the 1.21.1 one. Forge 1.20.1's dispenser shears through
// vanilla's Shearable, not its own IForgeShearable: it asks readyForShearing, then calls shear. An unfamiliar animal, or
// a sheep already sheared today, isn't ready.
@Mixin(ShearsDispenseItemBehavior.class)
public class ShearsDispenseMixin {
    @ModifyExpressionValue(
            method = "tryShearLivingEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Shearable;readyForShearing()Z"
            )
    )
    private static boolean barnyardromance$familiar(boolean shearable, @Local LivingEntity entity) {
        return shearable && !(entity instanceof Animal animal && (ShearActions.denied(animal) || ShearActions.withholds(animal)));
    }

    // Sheep count themselves in SheepShearMixin; any other tracked animal is counted once it has been sheared (a
    // mooshroom is already a cow by then).
    @Inject(
            method = "tryShearLivingEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Shearable;shear(Lnet/minecraft/sounds/SoundSource;)V",
                    shift = At.Shift.AFTER
            )
    )
    private static void barnyardromance$countShearing(ServerLevel level, BlockPos pos, CallbackInfoReturnable<Boolean> callback, @Local LivingEntity entity) {
        if (entity instanceof Animal animal && !(animal instanceof Sheep)) {
            ShearActions.recordAfterShear(animal);
        }
    }
}
