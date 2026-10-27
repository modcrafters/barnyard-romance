package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.pregnancy.MatingGate;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// These override canMate without calling super, so AnimalBreedingMixin never sees them.
@Mixin({Horse.class, Donkey.class, Llama.class, Camel.class, Wolf.class})
public class MateOverridesMixin {
    @Inject(method = "canMate", at = @At("RETURN"), cancellable = true)
    private void barnyardromance$sexes(Animal partner, CallbackInfoReturnable<Boolean> callback) {
        if (Boolean.TRUE.equals(callback.getReturnValue()) && !MatingGate.allows((Animal) (Object) this, partner)) {
            callback.setReturnValue(false);
        }
    }
}
