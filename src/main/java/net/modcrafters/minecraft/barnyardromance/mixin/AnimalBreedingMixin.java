package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.client.AnimalHearts;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.pregnancy.MatingGate;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyController;
import net.modcrafters.minecraft.barnyardromance.pregnancy.VanillaBirth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public class AnimalBreedingMixin {
    @Inject(method = "setInLove", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$love(Player player, CallbackInfo callback) {
        if (!FamiliarityActions.allowsLove((Animal) (Object) this)) {
            callback.cancel();
        }
    }

    @Inject(method = "canMate", at = @At("RETURN"), cancellable = true)
    private void barnyardromance$sexes(Animal partner, CallbackInfoReturnable<Boolean> callback) {
        if (Boolean.TRUE.equals(callback.getReturnValue()) && !MatingGate.allows((Animal) (Object) this, partner)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "spawnChildFromBreeding", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$pregnancy(ServerLevel level, Animal partner, CallbackInfo callback) {
        if (PregnancyController.replaceBirth((Animal) (Object) this, partner, level)) {
            callback.cancel();
        }
    }

    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("TAIL"))
    private void barnyardromance$vanillaBirth(ServerLevel level, Animal partner, @Nullable AgeableMob baby, CallbackInfo callback) {
        VanillaBirth.born((Animal) (Object) this, partner, baby);
    }

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    //? if >=1.21.2 {
    private void barnyardromance$birth(ServerLevel level, CallbackInfo callback) {
        Animal self = (Animal) (Object) this;
    //?} else {
    /*private void barnyardromance$birth(CallbackInfo callback) {
        Animal self = (Animal) (Object) this;
        ServerLevel level = (ServerLevel) self.level();
    *///?}
        PregnancyController.catchUp(self, level);
        AnimalHearts.serverTick(self, level);
        AgeController.serverTick(self, level);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void barnyardromance$pregnancyHearts(CallbackInfo callback) {
        AnimalHearts.clientTick((Animal) (Object) this);
    }
}
