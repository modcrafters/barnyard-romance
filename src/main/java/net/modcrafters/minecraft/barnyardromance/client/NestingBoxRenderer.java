package net.modcrafters.minecraft.barnyardromance.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.modcrafters.minecraft.barnyardromance.nest.NestingBoxBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// The hen sitting in a nesting box, drawn with her own model through the real hole in the front. She sits on the
// hay with her legs in it, facing out. versions/1.21.1 has the same renderer for 1.21.1's drawing calls.
public class NestingBoxRenderer implements BlockEntityRenderer<NestingBoxBlockEntity, NestingBoxRenderer.State> {
    private final EntityRenderDispatcher entities;

    public NestingBoxRenderer(BlockEntityRendererProvider.Context context) {
        this.entities = context.entityRenderer();
    }

    public static class State extends BlockEntityRenderState {
        public @Nullable EntityRenderState hen;
        public double sinkBy;
        public float scale;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(NestingBoxBlockEntity box, State state, float partialTicks, Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(box, state, partialTicks, camera, breaking);
        Entity hen = NestingBoxHen.sitting(box);
        if (hen == null) {
            state.hen = null;
            return;
        }
        state.hen = this.entities.extractEntity(hen, partialTicks);
        state.hen.lightCoords = state.lightCoords;
        state.scale = NestingBoxHen.fit(hen);
        state.sinkBy = NestingBoxHen.legs(hen);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.hen == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, NestingBoxHen.HAY_TOP, 0.5);
        poseStack.scale(state.scale, state.scale, state.scale);
        this.entities.submit(state.hen, camera, 0.0, -state.sinkBy, 0.0, poseStack, collector);
        poseStack.popPose();
    }
}
