package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.TraderLlama;
//? if >=1.21.2
import net.minecraft.world.entity.EntitySpawnReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Vanilla's trader llama foals are trader llamas, which despawn. Here they are ordinary llamas, so a player can breed
// a trader's stock into a herd.
@Mixin(TraderLlama.class)
public class TraderLlamaOffspringMixin {
    @Inject(method = "makeNewLlama", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$llamaFoal(CallbackInfoReturnable<Llama> callback) {
        Llama self = (Llama) (Object) this;
        //? if >=1.21.2 {
        callback.setReturnValue(EntityTypes.LLAMA.create(self.level(), EntitySpawnReason.BREEDING));
        //?} else
        //callback.setReturnValue(EntityTypes.LLAMA.create(self.level()));
    }
}
