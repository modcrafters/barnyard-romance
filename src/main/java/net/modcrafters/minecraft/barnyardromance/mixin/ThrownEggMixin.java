package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.modcrafters.minecraft.barnyardromance.eggs.ThrownEggs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Thrown eggs follow the mod's rules (ThrownEggs): plain eggs never hatch, fertilized ones sometimes hatch a poor
// chick. Vanilla's own one-in-eight roll is made to miss so it never adds chicks of its own.
@Mixin(ThrownEgg.class)
public class ThrownEggMixin {
    @Inject(method = "onHit", at = @At("HEAD"))
    private void barnyardromance$hatch(HitResult result, CallbackInfo callback) {
        ThrownEgg self = (ThrownEgg) (Object) this;
        if (self.level() instanceof ServerLevel level) {
            ThrownEggs.land(level, self.position(), self.getItem(), self.getRandom());
        }
    }

    @ModifyExpressionValue(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 0))
    private int barnyardromance$noVanillaChicks(int roll) {
        return ThrownEggs.blocksVanillaChicks(((ThrownEgg) (Object) this).getItem()) ? 1 : roll;
    }
}
