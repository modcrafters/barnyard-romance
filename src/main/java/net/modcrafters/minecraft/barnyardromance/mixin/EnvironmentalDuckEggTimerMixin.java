package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.core.compat.ModFields;
import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Environmental's ducks drop an egg on their own timer, males too. Duck (an Animal) declares aiStep, and its public
// `timeUntilNextEgg` counts down in it, then spawns getEggItem(). Read from the jar of 1.21.1 (NeoForge), the only build
// there is. Ducks that lay by the mod's rules (EggLaying) never use it, so it is held off. CompatMixinPlugin only
// registers this when the mod is there, and require = 0 means a future version that renames the method loses this hook
// instead of crashing the game.
@Pseudo
@Mixin(targets = "com.teamabnormals.environmental.common.entity.animal.Duck", remap = false)
public class EnvironmentalDuckEggTimerMixin {
    @Inject(method = "aiStep", at = @At("HEAD"), require = 0)
    private void barnyardromance$holdEggTimer(CallbackInfo callback) {
        Animal self = (Animal) (Object) this;
        if (!self.level().isClientSide() && EggLaying.laysEggs(self)) {
            ModFields.holdInt(self, "timeUntilNextEgg", 6000);
        }
    }
}
