package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.core.AnimalPhase;
import net.modcrafters.minecraft.barnyardromance.core.AnimalTicks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
public class AgeableMobMixin {
    @Inject(method = "ageBoundaryReached", at = @At("TAIL"))
    private void barnyardromance$onAgeBoundary(CallbackInfo callback) {
        if ((Object) this instanceof Animal animal && animal.level() instanceof ServerLevel level) {
            AnimalTicks.run(AnimalPhase.AGE_BOUNDARY, animal, level);
        }
    }
}
