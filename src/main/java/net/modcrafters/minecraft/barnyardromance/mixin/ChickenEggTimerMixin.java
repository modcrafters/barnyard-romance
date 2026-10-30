package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.minecraft.world.entity.animal.chicken.Chicken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Hens that lay by the mod's daily rules (EggLaying) never lay by vanilla's timer, so it is held off.
@Mixin(Chicken.class)
public class ChickenEggTimerMixin {
    private static final int HELD_OFF = 6000;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void barnyardromance$holdEggTimer(CallbackInfo callback) {
        Chicken self = (Chicken) (Object) this;
        if (!self.level().isClientSide() && EggLaying.laysEggs(self)) {
            self.eggTime = HELD_OFF;
        }
    }
}
