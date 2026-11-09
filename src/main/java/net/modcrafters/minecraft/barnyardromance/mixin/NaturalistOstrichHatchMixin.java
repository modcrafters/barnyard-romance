package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.modcrafters.minecraft.barnyardromance.advancement.BarnyardAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

// Naturalist's ostriches hatch from its own egg block, which the mod leaves alone; this only sees the chick come out,
// for the advancements. NaturalistMixinPlugin only registers this when Naturalist is there, and require = 0 means a
// future Naturalist that changes the method loses this hook instead of crashing the game.
@Pseudo
@Mixin(targets = "com.crispytwig.naturalist.world.level.block.OstrichEggBlock", remap = false)
public class NaturalistOstrichHatchMixin {
    @WrapOperation(
            method = "randomTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            require = 0
    )
    private boolean barnyardromance$hatched(ServerLevel level, Entity entity, Operation<Boolean> original) {
        boolean added = original.call(level, entity);
        if (added && entity instanceof Animal chick) {
            BarnyardAdvancements.hatchedFromNaturalistEgg(level, chick);
        }
        return added;
    }
}
