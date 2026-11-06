package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.pregnancy.BreedingStatus;
import net.modcrafters.minecraft.barnyardromance.rivalry.Courtship;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public class AnimalLoveMixin {
    @Inject(method = "setInLove", at = @At("TAIL"))
    private void barnyardromance$loveStarts(Player player, CallbackInfo callback) {
        BreedingStatus.inLove((Animal) (Object) this);
        Courtship.start((Animal) (Object) this);
    }

    @Inject(method = "resetLove", at = @At("TAIL"))
    private void barnyardromance$loveEnds(CallbackInfo callback) {
        BreedingStatus.outOfLove((Animal) (Object) this);
    }

    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("TAIL"))
    private void barnyardromance$rest(ServerLevel level, Animal partner, AgeableMob offspring, CallbackInfo callback) {
        Animal self = (Animal) (Object) this;
        self.setAge(Courtship.restAfterMating(self, self.getAge()));
        partner.setAge(Courtship.restAfterMating(partner, partner.getAge()));
        BreedingStatus.resting(self);
        BreedingStatus.resting(partner);
    }
}
