package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
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
        if ((Object) this instanceof Animal animal) {
            AnimalSex.onAgeBoundary(animal);
        }
    }
}
