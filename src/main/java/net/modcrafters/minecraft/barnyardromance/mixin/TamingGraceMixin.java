package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// A wolf or cat tamed some other way (another mod, a command, vanilla before the mod) starts out at least familiar,
// so it isn't grumpy from the moment it's tamed. Loading a tamed pet doesn't go through here.
@Mixin(TamableAnimal.class)
public class TamingGraceMixin {
    @Inject(method = "tame", at = @At("TAIL"))
    private void barnyardromance$familiar(Player player, CallbackInfo callback) {
        FamiliarityCare.raiseToThreshold((TamableAnimal) (Object) this);
    }
}
