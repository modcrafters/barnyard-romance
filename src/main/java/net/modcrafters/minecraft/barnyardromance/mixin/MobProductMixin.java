package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.herd.HerdItems;
import net.modcrafters.minecraft.barnyardromance.milk.MilkActions;
import net.modcrafters.minecraft.barnyardromance.shear.ShearActions;
import net.modcrafters.minecraft.barnyardromance.shear.SheepColours;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
//? if >=1.21.11
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobProductMixin {
    // Horses and camels act on most held items themselves (rear up, open inventory), and tamed pets sit or stand,
    // so our tools go first.
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    //? if >=1.21.11 {
    private void barnyardromance$tools(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> callback) {
    //?} else
    //private void barnyardromance$tools(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
        ItemStack stack = player.getItemInHand(hand);
        if (!((Object) this instanceof Animal animal) || !HerdItems.isTool(stack)) {
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
    //? if >=1.21.11 {
    private void barnyardromance$products(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> callback) {
    //?} else
    //private void barnyardromance$products(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
        if (!((Object) this instanceof Animal animal)) {
            return;
        }
        InteractionResult washing = SheepColours.wash(animal, player, hand);
        if (washing != null) {
            callback.setReturnValue(washing);
            return;
        }
        InteractionResult feeding = FamiliarityActions.interact(animal, player, hand);
        if (feeding != null) {
            callback.setReturnValue(feeding);
            return;
        }
        InteractionResult bone = FamiliarityActions.blocksVanillaTaming(animal, player, player.getItemInHand(hand));
        if (bone != null) {
            callback.setReturnValue(bone);
            return;
        }
        InteractionResult unfamiliar = FamiliarityActions.refusesProduct(animal, player, player.getItemInHand(hand));
        if (unfamiliar != null) {
            callback.setReturnValue(unfamiliar);
            return;
        }
        InteractionResult shorn = ShearActions.refuses(animal, player, player.getItemInHand(hand));
        if (shorn != null) {
            callback.setReturnValue(shorn);
            return;
        }
        // A bowl held out to a stew animal can be turned away like a bucket; otherwise the animal's own stew comes.
        if (!animal.isBaby() && player.getItemInHand(hand).is(Items.BOWL) && MilkActions.givesStew(animal)) {
            InteractionResult refused = MilkActions.refusesStew(animal, player);
            if (refused != null) {
                callback.setReturnValue(refused);
                return;
            }
        }
        InteractionResult milking = MilkActions.milk(animal, player, hand);
        if (milking != null) {
            callback.setReturnValue(milking);
        }
    }

    // Stew is milking by another name, so it costs the same familiarity. The stew itself stays the animal's own.
    @WrapOperation(
            method = "interact",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
            )
    )
    private InteractionResult barnyardromance$stewCost(Mob instance, Player player, InteractionHand hand, Operation<InteractionResult> original) {
        // The hand changes once the bowl is filled, so ask before.
        boolean bowl = player.getItemInHand(hand).is(Items.BOWL);
        InteractionResult result = original.call(instance, player, hand);
        if (bowl && instance instanceof Animal animal && !animal.isBaby() && result.consumesAction()
                && !animal.level().isClientSide() && MilkActions.givesStew(animal)) {
            FamiliarityCare.spend(animal, BarnyardConfig.current().tuning().familiarityMilkingCost());
        }
        return result;
    }
}
