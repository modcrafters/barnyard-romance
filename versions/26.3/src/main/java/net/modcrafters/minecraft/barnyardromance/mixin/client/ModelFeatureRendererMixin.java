package net.modcrafters.minecraft.barnyardromance.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.modcrafters.minecraft.barnyardromance.look.AnimalLookState;
import net.modcrafters.minecraft.barnyardromance.look.HornDraw;
import net.modcrafters.minecraft.barnyardromance.look.HornRenderer;
import net.modcrafters.minecraft.barnyardromance.look.ModelParts;
import net.modcrafters.minecraft.barnyardromance.look.VanillaHornHider;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

// 26.3 only: models are drawn in a later pass, right after their pose is set up, so vanilla horns are hidden here, and
// the mod's horns are drawn here right after the model: animation mods such as Entity Model Features pose the model only
// while it is drawn, and the parts keep that pose until the next setupAnim.
@Mixin(ModelFeatureRenderer.class)
public abstract class ModelFeatureRendererMixin {
    @WrapOperation(
            method = "prepareModel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            )
    )
    private void barnyardromance$hideVanillaHorns(
            Model<?> model,
            PoseStack poseStack,
            VertexConsumer buffer,
            int light,
            int overlay,
            int color,
            Operation<Void> original,
            @Local(argsOnly = true) ModelFeatureRenderer.Submit<?> submit
    ) {
        HornDraw draw = submit.state() instanceof AnimalLookState look ? look.barnyardromance$horns() : null;
        if (draw == null) {
            original.call(model, poseStack, buffer, light, overlay, color);
            return;
        }
        Runnable undo = VanillaHornHider.hide(path -> ModelParts.find(model, path), draw.horns().file().hide());
        try {
            original.call(model, poseStack, buffer, light, overlay, color);
        } finally {
            undo.run();
        }
        barnyardromance$drawHorns(draw, model, poseStack, submit);
    }

    // Only on the renderer's own model: layers (a sheep's wool) draw other models with the same state. A glowing
    // animal's outline is a second submit of that model, with an outline render type, so the outline takes in the horns.
    @Unique
    private void barnyardromance$drawHorns(HornDraw draw, Model<?> model, PoseStack poseStack, ModelFeatureRenderer.Submit<?> submit) {
        if (draw.parts().isEmpty()
                || !(submit.state() instanceof LivingEntityRenderState living)
                || living.isInvisible
                || ((AnimalLookState) living).barnyardromance$hornModel() != model) {
            return;
        }
        boolean outline = submit.renderType().isOutline();
        RenderType type = outline ? RenderTypes.outline(draw.horns().texture()) : RenderTypes.entityCutout(draw.horns().texture());
        VertexConsumer horns = ((RenderTypeFeatureRendererInvoker) this).barnyardromance$getVertexBuilder(type);
        HornRenderer.render(draw, model, poseStack, horns, submit.lightCoords(), submit.overlayCoords(), outline ? submit.tintedColor() : -1);
    }
}
