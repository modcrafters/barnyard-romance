package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.milk.MilkActions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobProductMixin {
    // Horses and camels act on most held items themselves (rear up, open inventory), so our tools go first.
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$tools(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> callback) {
        ItemStack stack = player.getItemInHand(hand);
        if (!((Object) this instanceof Animal animal) || !BarnyardItems.isTool(stack)) {
            return;
        }
        InteractionResult result = stack.interactLivingEntity(player, animal, hand);
        if (result.consumesAction()) {
            callback.setReturnValue(result);
        }
    }

    @Inject(
            method = "interact",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
            ),
            cancellable = true
    )
    private void barnyardromance$products(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> callback) {
        if (!((Object) this instanceof Animal animal)) {
            return;
        }
        InteractionResult feeding = FamiliarityActions.interact(animal, player, hand);
        if (feeding != null) {
            callback.setReturnValue(feeding);
            return;
        }
        if (FamiliarityActions.deniesProduct(animal, player.getItemInHand(hand))) {
            callback.setReturnValue(InteractionResult.FAIL);
            return;
        }
        InteractionResult milking = MilkActions.milk(animal, player, hand);
        if (milking != null) {
            callback.setReturnValue(milking);
        }
    }
}
