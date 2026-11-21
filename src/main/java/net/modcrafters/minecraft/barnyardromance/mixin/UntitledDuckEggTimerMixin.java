package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.core.compat.ModFields;
import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Untitled Duck Mod's ducks and geese drop an egg on their own timer, males too. WaterfowlEntity (the mod's base class
// of DuckEntity and GooseEntity, a TamableAnimal) declares aiStep, and its `protected int eggLayTime` counts down in
// it, then spawns getEggItem(). Those that lay by the mod's rules (EggLaying) never use it, so it is held off.
// Read from the jars of 1.21.1 (NeoForge) and 26.3 (NeoForge and Fabric): the class, aiStep and eggLayTime are the same
// in all three. DuckEntity and GooseEntity override aiStep too, but call super.aiStep() first, so this reaches them.
// CompatMixinPlugin only registers this when the mod is there, and require = 0 means a future version that renames the
// method loses this hook instead of crashing the game.
@Pseudo
@Mixin(targets = "net.untitledduckmod.common.entity.WaterfowlEntity", remap = false)
public class UntitledDuckEggTimerMixin {
    @Inject(method = "aiStep", at = @At("HEAD"), require = 0)
    private void barnyardromance$holdEggTimer(CallbackInfo callback) {
        Animal self = (Animal) (Object) this;
        if (!self.level().isClientSide() && EggLaying.laysEggs(self)) {
            ModFields.holdInt(self, "eggLayTime", 6000);
        }
    }
}
