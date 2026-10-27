package net.modcrafters.minecraft.barnyardromance.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.modcrafters.minecraft.barnyardromance.look.AnimalLooks;
import net.modcrafters.minecraft.barnyardromance.look.HornDraw;
import net.modcrafters.minecraft.barnyardromance.look.HornRenderer;
import net.modcrafters.minecraft.barnyardromance.look.ModelParts;
import net.modcrafters.minecraft.barnyardromance.look.VanillaHornHider;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 1.21.1's copy: render() draws the model straight away, so the belly, hiding and horns all happen inside it.
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow
    protected M model;

    // Worked out once at the start of each render; only the render thread draws.
    @Unique
    private HornDraw barnyardromance$horns;

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void barnyardromance$workOutHorns(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        barnyardromance$horns = AnimalLooks.horns(entity);
    }

    // After the renderer's own scale, X is the animal's left-right axis, so this widens the body and not its length.
    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;scale(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;F)V",
                    shift = At.Shift.AFTER
            )
    )
    private void barnyardromance$widenBelly(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        float belly = AnimalLooks.bellyWiden(entity);
        if (belly != 1.0f) {
            poseStack.scale(belly, 1.0f, 1.0f);
        }
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            )
    )
    private void barnyardromance$hideVanillaHorns(
            EntityModel<?> drawn,
            PoseStack poseStack,
            VertexConsumer buffer,
            int light,
            int overlay,
            int color,
            Operation<Void> original
    ) {
        HornDraw draw = barnyardromance$horns;
        if (draw == null) {
            original.call(drawn, poseStack, buffer, light, overlay, color);
            return;
        }
        Runnable undo = VanillaHornHider.hide(path -> ModelParts.find(drawn, path), draw.horns().file().hide());
        try {
            original.call(drawn, poseStack, buffer, light, overlay, color);
        } finally {
            undo.run();
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V")
    )
    private void barnyardromance$renderHorns(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        HornDraw draw = barnyardromance$horns;
        barnyardromance$horns = null;
        if (draw == null || draw.parts().isEmpty() || entity.isInvisible()) {
            return;
        }
        HornRenderer.render(draw, model, poseStack, buffers, light, LivingEntityRenderer.getOverlayCoords(entity, 0.0f));
    }
}
