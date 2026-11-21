package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.shear.SheepColours;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// With sheepDyeing off, dye does nothing on a tracked sheep and isn't used up.
@Mixin(DyeItem.class)
public class SheepDyeMixin {
    @Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
    private void barnyardromance$noDye(ItemStack stack, Player player, LivingEntity target, InteractionHand hand,
                                       CallbackInfoReturnable<InteractionResult> callback) {
        if (target instanceof Sheep sheep && SheepColours.refusesDye(sheep)) {
            callback.setReturnValue(InteractionResult.PASS);
        }
    }
}
