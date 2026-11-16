package net.modcrafters.minecraft.barnyardromance.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// 26.3 only: a buffer of another render type in the group being built, so horns can be drawn in the model pass with
// their own texture.
@Mixin(RenderTypeFeatureRenderer.class)
public interface RenderTypeFeatureRendererInvoker {
    @Invoker("getVertexBuilder")
    VertexConsumer barnyardromance$getVertexBuilder(RenderType renderType);
}
