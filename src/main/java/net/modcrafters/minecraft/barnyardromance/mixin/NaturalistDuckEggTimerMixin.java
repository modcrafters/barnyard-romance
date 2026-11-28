package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.core.compat.ModFields;
import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Naturalist's ducks drop an egg on their own timer, males too. Those that lay by the mod's rules (EggLaying) never use
// it, so it is held off. CompatMixinPlugin only registers this when Naturalist is there, and require = 0 means a
// future Naturalist that renames the method loses this hook instead of crashing the game.
// On Forge 1.20.1 the mod's classes run with Minecraft's SRG names in a real game (Mojang's in the dev runs), and this
// @Pseudo mixin's selectors aren't remapped, so that build names the method both ways: aiStep is m_8107_ there.
@Pseudo
@Mixin(targets = "com.crispytwig.naturalist.world.entity.animal.duck.Duck", remap = false)
public class NaturalistDuckEggTimerMixin {
    //? if >=1.20.5 {
    @Inject(method = "aiStep", at = @At("HEAD"), require = 0)
    //?} else
    //@Inject(method = {"aiStep", "m_8107_"}, at = @At("HEAD"), require = 0)
    private void barnyardromance$holdEggTimer(CallbackInfo callback) {
        Animal self = (Animal) (Object) this;
        if (!self.level().isClientSide() && EggLaying.laysEggs(self)) {
            ModFields.holdInt(self, "eggTime", 6000);
        }
    }
}
