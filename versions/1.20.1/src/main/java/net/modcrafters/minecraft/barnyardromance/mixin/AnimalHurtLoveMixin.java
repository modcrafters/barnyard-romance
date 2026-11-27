package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.pregnancy.BreedingStatus;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 1.20.1 only: a hurt animal's love ends, as on the newer versions, but 1.20.1's hurt clears the field itself instead of
// calling resetLove (where AnimalLoveMixin records it), so the record is cleared here too.
@Mixin(Animal.class)
public class AnimalHurtLoveMixin {
    @Inject(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/animal/Animal;inLove:I", opcode = Opcodes.PUTFIELD))
    private void barnyardromance$hurtEndsLove(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        BreedingStatus.outOfLove((Animal) (Object) this);
    }
}
