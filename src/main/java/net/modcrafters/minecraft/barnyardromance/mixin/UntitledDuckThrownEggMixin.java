package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.modcrafters.minecraft.barnyardromance.compat.untitledduckmod.UntitledDuckModFeature;
import net.modcrafters.minecraft.barnyardromance.eggs.ThrownEggs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Untitled Duck Mod's thrown eggs are WaterfowlEggEntity, a ThrowableItemProjectile whose onHit hatches a duckling
// (or four) at one in eight: the first RandomSource.nextInt(I)I in it is nextInt(8), and a second nextInt(32) comes
// after it. The same in the jars of 1.21.1 (NeoForge) and 26.3 (NeoForge and Fabric). Its eggs are the items
// untitledduckmod:duck_egg and untitledduckmod:goose_egg, and the entity's getItem() is the thrown stack.
// Like ThrownEggMixin, this lets the mod's rules land the egg, and makes the mod's one-in-eight roll miss while the
// species lays by those rules. A species switched off keeps the mod's hatching. require = 0 loses a hook the mod
// renames instead of crashing the game; the plugin only lists this when the mod is there.
// On Forge 1.20.1 the mod's classes run with Minecraft's SRG names in a real game (Mojang's in the dev runs), and this
// @Pseudo mixin's selectors aren't remapped, so that build names onHit both ways (m_6532_ there) and has the refmap remap
// the nextInt call (remap = true on its @At).
@Pseudo
@Mixin(targets = "net.untitledduckmod.common.entity.WaterfowlEggEntity", remap = false)
public class UntitledDuckThrownEggMixin {
    //? if >=1.20.5 {
    @Inject(method = "onHit", at = @At("HEAD"), require = 0)
    //?} else
    //@Inject(method = {"onHit", "m_6532_"}, at = @At("HEAD"), require = 0)
    private void barnyardromance$hatch(HitResult result, CallbackInfo callback) {
        ThrowableItemProjectile self = (ThrowableItemProjectile) (Object) this;
        if (self.level() instanceof ServerLevel level) {
            ThrownEggs.land(level, self.position(), self.getItem(), self.getRandom());
        }
    }

    //? if >=1.20.5 {
    @ModifyExpressionValue(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 0), require = 0)
    //?} else
    //@ModifyExpressionValue(method = {"onHit", "m_6532_"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 0, remap = true), require = 0)
    private int barnyardromance$noModDucklings(int roll) {
        ThrowableItemProjectile self = (ThrowableItemProjectile) (Object) this;
        String species = switch (BuiltInRegistries.ITEM.getKey(self.getItem().getItem()).toString()) {
            case "untitledduckmod:duck_egg" -> UntitledDuckModFeature.DUCK;
            case "untitledduckmod:goose_egg" -> UntitledDuckModFeature.GOOSE;
            default -> null;
        };
        return species != null && ThrownEggs.blocksModChicks(species) ? 1 : roll;
    }
}
