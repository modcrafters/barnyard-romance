package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.modcrafters.minecraft.barnyardromance.eggs.ThrownEggs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Autumnity's thrown turkey eggs are ThrownTurkeyEgg, a ThrowableItemProjectile whose onHit hatches a poult (or four)
// at one in eight: the first RandomSource.nextInt(I)I in it is nextInt(8), and a second nextInt(32) comes after it. Read
// from the jar of 1.21.1 (NeoForge), the only build there is. Only turkeys throw it, so the species is always
// autumnity:turkey. Like ThrownEggMixin, this lets the mod's rules land the egg, and makes the mod's one-in-eight roll
// miss while turkeys lay by those rules; a turkey a pack switches off keeps the mod's hatching. require = 0 loses a hook
// the mod renames instead of crashing the game; the plugin only lists this when the mod is there.
@Pseudo
@Mixin(targets = "com.teamabnormals.autumnity.common.entity.projectile.ThrownTurkeyEgg", remap = false)
public class AutumnityThrownEggMixin {
    @Inject(method = "onHit", at = @At("HEAD"), require = 0)
    private void barnyardromance$hatch(HitResult result, CallbackInfo callback) {
        ThrowableItemProjectile self = (ThrowableItemProjectile) (Object) this;
        if (self.level() instanceof ServerLevel level) {
            ThrownEggs.land(level, self.position(), self.getItem(), self.getRandom());
        }
    }

    @ModifyExpressionValue(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 0), require = 0)
    private int barnyardromance$noModHatchlings(int roll) {
        return ThrownEggs.blocksModChicks("autumnity:turkey") ? 1 : roll;
    }
}
