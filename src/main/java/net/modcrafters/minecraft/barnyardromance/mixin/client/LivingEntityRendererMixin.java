package net.modcrafters.minecraft.barnyardromance.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.modcrafters.minecraft.barnyardromance.look.AnimalLookState;
import net.modcrafters.minecraft.barnyardromance.look.AnimalLooks;
import net.modcrafters.minecraft.barnyardromance.look.HornDraw;
import net.modcrafters.minecraft.barnyardromance.look.HornRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Widens pregnant females and adds horns. versions/1.21.1 has its own copy, where render() draws straight away.
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Shadow
    protected M model;

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void barnyardromance$extractLooks(T entity, S state, float partialTicks, CallbackInfo ci) {
        AnimalLookState look = (AnimalLookState) state;
        look.barnyardromance$setBelly(AnimalLooks.bellyWiden(entity));
        look.barnyardromance$setHorns(AnimalLooks.horns(entity));
    }

    // After the renderer's own scale, X is the animal's left-right axis, so this widens the body and not its length.
    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;scale(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void barnyardromance$widenBelly(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        float belly = ((AnimalLookState) state).barnyardromance$belly();
        if (belly != 1.0f) {
            poseStack.scale(belly, 1.0f, 1.0f);
        }
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V")
    )
    private void barnyardromance$submitHorns(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        HornDraw draw = ((AnimalLookState) state).barnyardromance$horns();
        if (draw == null || draw.parts().isEmpty() || state.isInvisible) {
            return;
        }
        // Layers set this up too, but only when the renderer has any; the horns need the head where it is now.
        model.setupAnim(state);
        HornRenderer.submit(draw, model, poseStack, collector, state.lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0.0f));
    }
}
