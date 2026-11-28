package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.event.Events;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

// Naturalist's ostriches hatch from its own egg block, which the mod leaves alone; this only sees the chick come out,
// for the advancements. CompatMixinPlugin only registers this when Naturalist is there, and require = 0 means a
// future Naturalist that changes the method loses this hook instead of crashing the game.
// On Forge 1.20.1 the mod's classes run with Minecraft's SRG names in a real game (Mojang's in the dev runs), and this
// @Pseudo mixin's selectors aren't remapped, so that build names randomTick both ways (m_213898_ there) and has the
// refmap remap the addFreshEntity call (remap = true on its @At).
@Pseudo
@Mixin(targets = "com.crispytwig.naturalist.world.level.block.OstrichEggBlock", remap = false)
public class NaturalistOstrichHatchMixin {
    @WrapOperation(
            //? if >=1.20.5 {
            method = "randomTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            //?} else {
            /*method = {"randomTick", "m_213898_"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", remap = true),
            *///?}
            require = 0
    )
    private boolean barnyardromance$hatched(ServerLevel level, Entity entity, Operation<Boolean> original) {
        boolean added = original.call(level, entity);
        if (added && entity instanceof Animal chick) {
            Events.post(new AnimalEvent.Hatched(level, chick, null, AnimalEvent.Hatch.NATURALIST_EGG));
        }
        return added;
    }
}
