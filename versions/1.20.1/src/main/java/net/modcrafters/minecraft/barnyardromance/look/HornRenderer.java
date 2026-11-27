package net.modcrafters.minecraft.barnyardromance.look;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

// 1.20.1's copy: draws an animal's horns at once, attached to its model.
public final class HornRenderer {
    private HornRenderer() {
    }

    // The pose must be in model space, with the model's pose already set up for this animal.
    public static void render(HornDraw draw, EntityModel<?> model, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        if (draw.parts().isEmpty()) {
            return;
        }
        HornFile file = draw.horns().file();
        poseStack.pushPose();
        HornAttach attach = HornAttach.first(file.attach(), path -> ModelParts.find(model, path) != null);
        if (attach != null && ModelParts.poseTo(model, attach.path(), poseStack)) {
            poseStack.translate(attach.offset().x() / 16.0f, attach.offset().y() / 16.0f, attach.offset().z() / 16.0f);
            VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(draw.horns().texture()));
            for (LoadedHorns.Baked baked : draw.parts()) {
                poseStack.pushPose();
                baked.pose(poseStack, draw.growth());
                baked.model().render(poseStack, buffer, light, overlay);
                poseStack.popPose();
            }
        } else {
            AnimalLooks.warnOnce("attach " + draw.entityId(), "The model of {} has none of the parts {} to hang horns from", draw.entityId(), HornAttach.paths(file.attach()));
        }
        poseStack.popPose();
    }
}
