package net.modcrafters.minecraft.barnyardromance.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;

// The 1.21.1 copy of src/.../client/NestingBoxRenderer.
public class NestingBoxRenderer implements BlockEntityRenderer<NestingBoxBlockEntity> {
    private final EntityRenderDispatcher entities;

    public NestingBoxRenderer(BlockEntityRendererProvider.Context context) {
        this.entities = context.getEntityRenderer();
    }

    @Override
    public void render(NestingBoxBlockEntity box, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        Entity hen = NestingBoxHen.sitting(box);
        if (hen == null) {
            return;
        }
        float scale = NestingBoxHen.fit(hen);
        poseStack.pushPose();
        poseStack.translate(0.5, NestingBoxHen.HAY_TOP, 0.5);
        poseStack.scale(scale, scale, scale);
        this.entities.render(hen, 0.0, -NestingBoxHen.legs(hen), 0.0, hen.getYRot(), partialTick, poseStack, buffers, light);
        poseStack.popPose();
    }
}
