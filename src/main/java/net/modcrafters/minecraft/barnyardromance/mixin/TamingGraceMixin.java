package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// However a wolf or cat is tamed (familiarity, another mod, a command), it starts out a week's hunger above the
// threshold, so it isn't grumpy after its first missed meal. Loading a tamed pet doesn't go through here.
@Mixin(TamableAnimal.class)
public class TamingGraceMixin {
    @Inject(method = "tame", at = @At("TAIL"))
    private void barnyardromance$familiar(Player player, CallbackInfo callback) {
        FamiliarityCare.raiseToTamingGrace((TamableAnimal) (Object) this);
    }
}
