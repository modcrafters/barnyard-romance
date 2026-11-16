package net.modcrafters.minecraft.barnyardromance.client;

// Vanilla's inventory entity preview, facing straight out, drawn at the frame's partial tick instead of vanilla's 1.0.
// Animation packs such as Fresh Animations drive their poses from age and limb swing, which include the partial tick,
// so at a fixed 1.0 the preview only moves in tick-sized steps against values smoothed by the world draw, and shakes.
//? if >=26.1 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class EntityPreview {
    private EntityPreview() {
    }

    public static void draw(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int size, float offsetY, LivingEntity entity, float partialTick) {
        EntityRenderer<? super LivingEntity, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        EntityRenderState state = renderer.createRenderState(entity, partialTick);
        state.shadowPieces.clear();
        state.outlineColor = 0;
        if (state instanceof LivingEntityRenderState living) {
            living.bodyRot = 180.0F;
            living.yRot = 0.0F;
            living.xRot = 0.0F;
            living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
            living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
            living.scale = 1.0F;
        }
        Vector3f translation = new Vector3f(0.0F, state.boundingBoxHeight / 2.0F + offsetY, 0.0F);
        graphics.entity(state, size, translation, new Quaternionf().rotateZ((float) Math.PI), new Quaternionf(), x0, y0, x1, y1);
    }
}
//?} else {
/*import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

public final class EntityPreview {
    private EntityPreview() {
    }

    // Drawn at once, like 1.21.1's InventoryScreen.renderEntityInInventory. Between ticks the renderer blends each
    // rotation with its previous-tick value, so both are set to the preview's and put back afterwards.
    public static void draw(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int size, float offsetY, LivingEntity entity, float partialTick) {
        float bodyRot = entity.yBodyRot;
        float bodyRotO = entity.yBodyRotO;
        float yRot = entity.getYRot();
        float yRotO = entity.yRotO;
        float xRot = entity.getXRot();
        float xRotO = entity.xRotO;
        float headRot = entity.yHeadRot;
        float headRotO = entity.yHeadRotO;
        entity.yBodyRot = 180.0F;
        entity.yBodyRotO = 180.0F;
        entity.setYRot(180.0F);
        entity.yRotO = 180.0F;
        entity.setXRot(0.0F);
        entity.xRotO = 0.0F;
        entity.yHeadRot = 180.0F;
        entity.yHeadRotO = 180.0F;
        float entityScale = entity.getScale();
        float scale = size / entityScale;
        graphics.pose().pushPose();
        graphics.pose().translate((x0 + x1) / 2.0F, (y0 + y1) / 2.0F, 50.0F);
        graphics.pose().scale(scale, scale, -scale);
        graphics.pose().translate(0.0F, entity.getBbHeight() / 2.0F + offsetY * entityScale, 0.0F);
        graphics.pose().mulPose(new Quaternionf().rotateZ((float) Math.PI));
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.overrideCameraOrientation(new Quaternionf().rotateY((float) Math.PI));
        dispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, partialTick, graphics.pose(), graphics.bufferSource(), 15728880));
        graphics.flush();
        dispatcher.setRenderShadow(true);
        graphics.pose().popPose();
        Lighting.setupFor3DItems();
        entity.yBodyRot = bodyRot;
        entity.yBodyRotO = bodyRotO;
        entity.setYRot(yRot);
        entity.yRotO = yRotO;
        entity.setXRot(xRot);
        entity.xRotO = xRotO;
        entity.yHeadRot = headRot;
        entity.yHeadRotO = headRotO;
    }
}
*///?}
