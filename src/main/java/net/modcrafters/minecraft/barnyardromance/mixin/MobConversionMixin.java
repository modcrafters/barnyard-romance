package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.animal.Conversions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
//? if >=1.21.2 {
import net.minecraft.world.entity.ConversionParams;
//?} else
//import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// An animal that turns into another animal (a sheared mooshroom into a cow) keeps its record: it is copied onto the new
// animal just before that one joins the level, so its first tick has its traits, nothing rolls it new ones, and the
// herd book never sees a stranger or a death.
@Mixin(Mob.class)
public class MobConversionMixin {
    //? if >=1.21.2 {
    // The new mob is finished (and, once this returns, added) by the caller's own step, so the carry goes first.
    @WrapOperation(
            method = "convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ConversionParams$AfterConversion;finalizeConversion(Lnet/minecraft/world/entity/Mob;)V")
    )
    private void barnyardromance$carryRecord(ConversionParams.AfterConversion<Mob> step, Mob newMob, Operation<Void> original) {
        carry((Mob) (Object) this, newMob);
        original.call(step, newMob);
    }
    //?} else {
    /*// 1.21.1 adds the new mob inside convertTo, so the carry goes right before that.
    @WrapOperation(
            method = "convertTo(Lnet/minecraft/world/entity/EntityType;Z)Lnet/minecraft/world/entity/Mob;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean barnyardromance$carryRecord(Level level, net.minecraft.world.entity.Entity newMob, Operation<Boolean> original) {
        carry((Mob) (Object) this, newMob);
        return original.call(level, newMob);
    }
    *///?}

    // A herd book or data problem mustn't stop the conversion.
    private static void carry(Mob from, Object to) {
        if (from instanceof Animal oldAnimal && to instanceof Animal newAnimal && !oldAnimal.level().isClientSide()) {
            try {
                Conversions.carry(oldAnimal, newAnimal);
            } catch (RuntimeException exception) {
                Constants.LOG.warn("Failed to carry an animal's record across a conversion", exception);
            }
        }
    }
}
