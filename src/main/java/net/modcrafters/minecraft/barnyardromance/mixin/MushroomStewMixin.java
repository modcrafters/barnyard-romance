package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Stew from a mooshroom's bowl is milking by another name, so it costs the same familiarity. The stew itself stays vanilla.
@Mixin(MushroomCow.class)
public class MushroomStewMixin {
    @Unique
    private boolean barnyardromance$bowl;

    @Inject(method = "mobInteract", at = @At("HEAD"))
    private void barnyardromance$holdingBowl(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
        MushroomCow mooshroom = (MushroomCow) (Object) this;
        this.barnyardromance$bowl = !mooshroom.isBaby() && player.getItemInHand(hand).is(Items.BOWL);
    }

    @Inject(method = "mobInteract", at = @At("RETURN"))
    private void barnyardromance$stewCost(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
        MushroomCow mooshroom = (MushroomCow) (Object) this;
        if (this.barnyardromance$bowl && callback.getReturnValue().consumesAction() && !mooshroom.level().isClientSide()) {
            FamiliarityCare.spend(mooshroom, BarnyardConfig.current().tuning().familiarityMilkingCost());
        }
        this.barnyardromance$bowl = false;
    }
}
