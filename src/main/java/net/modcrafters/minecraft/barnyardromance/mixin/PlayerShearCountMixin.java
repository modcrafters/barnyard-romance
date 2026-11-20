package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
//? if >=1.21.11
import net.minecraft.world.phys.Vec3;
import net.modcrafters.minecraft.barnyardromance.shear.ShearActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Shears used by hand on a tracked animal that isn't a sheep: vanilla shears in the animal's own mobInteract, NeoForge
// in ShearsItem.interactLivingEntity once that passes. A right click on an entity goes through Player.interactOn on
// every target, so the once-a-day shearing is counted from there, whichever of the two did the shearing. Sheep count
// themselves in SheepShearMixin.
@Mixin(Player.class)
public class PlayerShearCountMixin {
    @Unique
    private Animal barnyardromance$shorn;
    @Unique
    private boolean barnyardromance$wasReady;

    @Inject(method = "interactOn", at = @At("HEAD"))
    //? if >=1.21.11 {
    private void barnyardromance$beforeShearing(Entity entity, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> callback) {
    //?} else
    //private void barnyardromance$beforeShearing(Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
        Player player = (Player) (Object) this;
        barnyardromance$shorn = null;
        if (player.level().isClientSide() || !(entity instanceof Animal animal) || animal instanceof Sheep
                || !player.getItemInHand(hand).is(Items.SHEARS) || !ShearActions.tracksShearing(animal)) {
            return;
        }
        barnyardromance$shorn = animal;
        barnyardromance$wasReady = ShearActions.readyForShearing(animal);
    }

    @Inject(method = "interactOn", at = @At("RETURN"))
    private void barnyardromance$afterShearing(CallbackInfoReturnable<InteractionResult> callback) {
        Animal animal = barnyardromance$shorn;
        barnyardromance$shorn = null;
        if (animal != null) {
            ShearActions.recordAfterInteract(animal, barnyardromance$wasReady, callback.getReturnValue());
        }
    }
}
