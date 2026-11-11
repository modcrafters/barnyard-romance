package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.event.Events;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// An animal's death, for the slaughter advancement. Both loaders end up here, whatever killed it. It hooks where die
// looks up the kill credit: past vanilla's guard against dying twice and past NeoForge's LivingDeathEvent, so a death
// another mod cancels earns nothing.
@Mixin(LivingEntity.class)
public class AnimalDeathMixin {
    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getKillCredit()Lnet/minecraft/world/entity/LivingEntity;", ordinal = 0))
    private void barnyardromance$died(DamageSource source, CallbackInfo callback) {
        if ((Object) this instanceof Animal animal && !animal.level().isClientSide()) {
            Events.post(new AnimalEvent.Died(animal, source));
        }
    }
}
