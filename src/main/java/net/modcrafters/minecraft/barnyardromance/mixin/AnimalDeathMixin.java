package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.advancement.BarnyardAdvancements;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// An animal's death, for the slaughter advancement. Both loaders end up here, whatever killed it.
@Mixin(LivingEntity.class)
public class AnimalDeathMixin {
    // Vanilla's own guard against dying twice.
    @Shadow
    protected boolean dead;

    @Inject(method = "die", at = @At("HEAD"))
    private void barnyardromance$died(DamageSource source, CallbackInfo callback) {
        if ((Object) this instanceof Animal animal && !animal.level().isClientSide() && !animal.isRemoved() && !dead) {
            BarnyardAdvancements.died(animal, source);
        }
    }
}
