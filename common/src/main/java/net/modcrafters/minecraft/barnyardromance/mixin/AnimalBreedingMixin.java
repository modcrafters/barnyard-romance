package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.MatingRules;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyController;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Animal.class)
public class AnimalBreedingMixin {
    @Inject(method = "canMate", at = @At("RETURN"), cancellable = true)
    private void barnyardromance$sexes(Animal partner, CallbackInfoReturnable<Boolean> callback) {
        if (!Boolean.TRUE.equals(callback.getReturnValue())) {
            return;
        }
        Animal self = (Animal) (Object) this;
        Optional<AnimalTraits> selfTraits = Services.DATA.get(self);
        Optional<AnimalTraits> partnerTraits = Services.DATA.get(partner);
        boolean allowed = MatingRules.canMate(
                selfTraits.map(AnimalTraits::sex),
                partnerTraits.map(AnimalTraits::sex),
                Services.DATA.getPregnancy(self).isPresent(),
                Services.DATA.getPregnancy(partner).isPresent()
        );
        if (!allowed) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "spawnChildFromBreeding", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$pregnancy(ServerLevel level, Animal partner, CallbackInfo callback) {
        if (PregnancyController.replaceBirth((Animal) (Object) this, partner, level)) {
            callback.cancel();
        }
    }

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void barnyardromance$birth(ServerLevel level, CallbackInfo callback) {
        PregnancyController.catchUp((Animal) (Object) this, level);
    }
}
