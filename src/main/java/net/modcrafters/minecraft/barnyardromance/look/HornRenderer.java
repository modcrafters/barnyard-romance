package net.modcrafters.minecraft.barnyardromance.look;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

// Sends an animal's horns to be drawn, attached to its model. versions/1.21.1 has its own copy that draws at once.
public final class HornRenderer {
    private HornRenderer() {
    }

    // The pose must be in model space, with the model's pose already set up for this animal.
    public static void submit(HornDraw draw, Model<?> model, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay) {
        if (draw.parts().isEmpty()) {
            return;
        }
        HornFile file = draw.horns().file();
        poseStack.pushPose();
        if (ModelParts.poseTo(model, file.attach(), poseStack)) {
            poseStack.translate(file.offset().x() / 16.0f, file.offset().y() / 16.0f, file.offset().z() / 16.0f);
            RenderType type = RenderTypes.entityCutout(draw.horns().texture());
            for (LoadedHorns.Baked baked : draw.parts()) {
                poseStack.pushPose();
                baked.pose(poseStack, draw.growth());
                collector.submitModelPart(baked.model(), poseStack, type, light, overlay, null);
                poseStack.popPose();
            }
        } else {
            AnimalLooks.warnOnce("attach " + draw.entityId(), "The model of {} has no part {} to hang horns from", draw.entityId(), String.join("/", file.attach()));
        }
        poseStack.popPose();
    }
}
