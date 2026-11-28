package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Grumpy;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// A grumpy cat won't sit or stand up when told to; see GrumpyWolfSitMixin.
@Mixin(Cat.class)
public class GrumpyCatSitMixin {
    // 1.20.1's Mixin annotation processor, which writes Forge's refmap, needs the call's owner.
    //? if >=1.20.5 {
    @WrapOperation(method = "mobInteract", at = @At(value = "INVOKE", target = "setOrderedToSit(Z)V"))
    //?} else
    //@WrapOperation(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Cat;setOrderedToSit(Z)V"))
    private void barnyardromance$refuseSit(Cat pet, boolean sit, Operation<Void> original, @Local(argsOnly = true) Player player) {
        if (!pet.level().isClientSide() && FamiliarityCare.grumpy(pet)) {
            Grumpy.refuse(pet, player, Grumpy.WONT_SIT);
            return;
        }
        original.call(pet, sit);
    }
}
