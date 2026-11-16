package net.modcrafters.minecraft.barnyardromance.look;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;

// Draws an animal's horns onto its model, in the model pass right after the model itself (ModelFeatureRendererMixin),
// so they follow the pose the model was just drawn with. versions/1.21.1 has its own copy.
public final class HornRenderer {
    private HornRenderer() {
    }

    // The pose must be in model space, and the buffer one for the horns' own texture.
    public static void render(HornDraw draw, Model<?> model, PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        if (draw.parts().isEmpty()) {
            return;
        }
        HornFile file = draw.horns().file();
        poseStack.pushPose();
        HornAttach attach = HornAttach.first(file.attach(), path -> ModelParts.find(model, path) != null);
        if (attach != null && ModelParts.poseTo(model, attach.path(), poseStack)) {
            poseStack.translate(attach.offset().x() / 16.0f, attach.offset().y() / 16.0f, attach.offset().z() / 16.0f);
            for (LoadedHorns.Baked baked : draw.parts()) {
                poseStack.pushPose();
                baked.pose(poseStack, draw.growth());
                baked.model().render(poseStack, buffer, light, overlay, color);
                poseStack.popPose();
            }
        } else {
            AnimalLooks.warnOnce("attach " + draw.entityId(), "The model of {} has none of the parts {} to hang horns from", draw.entityId(), HornAttach.paths(file.attach()));
        }
        poseStack.popPose();
    }
}
