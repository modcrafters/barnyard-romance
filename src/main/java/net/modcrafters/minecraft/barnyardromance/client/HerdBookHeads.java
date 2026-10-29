// 26.3 draws entities in screens from render states; 1.21.1 has no head close-ups yet, and its rows go without.
//? if >=26.1 {
package net.modcrafters.minecraft.barnyardromance.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.AgeableMob;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

// Draws a close-up of an animal's head into a small square of the herd book, from its own model: a loaded animal
// as it looks now, anything else as a stand-in of its species. The model's part named "head" (with its ears, horns
// and snout) is framed, so modded animals work as long as their model names it that.
public final class HerdBookHeads {
    // Turned a little, so the face doesn't look flat.
    private static final float TURN = 25.0F;
    private static final float FILL = 0.95F;
    private static final Map<String, LivingEntity> STAND_INS = new HashMap<>();
    // Head boxes per animal, so the renderer only has to be asked once; an empty array means no head part.
    private static final Map<LivingEntity, float[]> BOXES = new WeakHashMap<>();

    private HerdBookHeads() {
    }

    public static void draw(GuiGraphicsExtractor graphics, UUID id, String species, boolean kid, int x, int y, int size) {
        LivingEntity entity = entity(id, species, kid);
        if (entity == null) {
            return;
        }
        EntityRenderer<? super LivingEntity, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        EntityRenderState state = renderer.createRenderState(entity, 1.0F);
        state.shadowPieces.clear();
        state.outlineColor = 0;
        if (!(state instanceof LivingEntityRenderState living)) {
            return;
        }
        living.bodyRot = 180.0F + TURN;
        living.yRot = 0.0F;
        living.xRot = 0.0F;
        living.walkAnimationPos = 0.0F;
        living.walkAnimationSpeed = 0.0F;
        living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
        living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
        living.scale = 1.0F;
        float[] box = BOXES.computeIfAbsent(entity, key -> {
            float[] found = headBox(renderer, living);
            return found == null ? new float[0] : found;
        });
        if (box.length == 0) {
            box = null;
        }
        Vector3f translation;
        float scale;
        if (box != null) {
            float boxWidth = box[3] - box[0];
            float boxHeight = box[4] - box[1];
            scale = size * FILL / Math.max(boxWidth, boxHeight);
            translation = new Vector3f((box[0] + box[3]) / 2.0F, (box[1] + box[4]) / 2.0F, 0.0F);
        } else {
            float head = Math.clamp(entity.getBbWidth() * 0.9F, 0.45F, 1.2F);
            scale = size / head;
            translation = new Vector3f(0.0F, entity.getEyeHeight(), 0.0F);
        }
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        graphics.entity(living, scale, translation, rotation, null, x, y, x + size, y + size);
    }

    // The box around the model's "head" part (with its ears, horns and snout) in blocks, posed and turned like the drawing:
    // {minX, minY, minZ, maxX, maxY, maxZ}, or null when the model has no head part.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static float @Nullable [] headBox(EntityRenderer renderer, LivingEntityRenderState state) {
        // Let the renderer pick its model (variant, baby) and place it, and catch the first model it hands over.
        Model[] model = new Model[1];
        Matrix4f[] placed = new Matrix4f[1];
        SubmitNodeCollector[] recorder = new SubmitNodeCollector[1];
        recorder[0] = (SubmitNodeCollector) Proxy.newProxyInstance(HerdBookHeads.class.getClassLoader(), new Class<?>[] {SubmitNodeCollector.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("submitModel") && model[0] == null && args[0] instanceof Model submitted && args[2] instanceof PoseStack stack) {
                        model[0] = submitted;
                        placed[0] = new Matrix4f(stack.last().pose());
                        return null;
                    }
                    if (method.isDefault() && !method.getName().equals("submitModel")) {
                        return InvocationHandler.invokeDefault(proxy, method, args);
                    }
                    Class<?> type = method.getReturnType();
                    if (type.isInstance(proxy)) {
                        return proxy;
                    }
                    return type == boolean.class ? false : type == int.class ? 0 : type == float.class ? 0.0F : type == double.class ? 0.0 : type == long.class ? 0L : null;
                });
        try {
            renderer.submit(state, new PoseStack(), recorder[0], new CameraRenderState());
        } catch (RuntimeException e) {
            // Later layers (horns) may not like the recorder; the body model is already caught by then.
        }
        if (model[0] == null) {
            return null;
        }
        model[0].setupAnim(state);
        PoseStack pose = new PoseStack();
        pose.last().pose().set(placed[0]);
        float[] box = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        model[0].root().visit(pose, (cubePose, partPath, index, cube) -> {
            if (!(partPath + "/").contains("/head/")) {
                return;
            }
            for (int corner = 0; corner < 8; corner++) {
                Vector3f point = cubePose.pose().transformPosition(new Vector3f(
                        ((corner & 1) == 0 ? cube.minX : cube.maxX) / 16.0F,
                        ((corner & 2) == 0 ? cube.minY : cube.maxY) / 16.0F,
                        ((corner & 4) == 0 ? cube.minZ : cube.maxZ) / 16.0F));
                box[0] = Math.min(box[0], point.x);
                box[1] = Math.min(box[1], point.y);
                box[2] = Math.min(box[2], point.z);
                box[3] = Math.max(box[3], point.x);
                box[4] = Math.max(box[4], point.y);
                box[5] = Math.max(box[5], point.z);
            }
        });
        model[0].resetPose();
        return box[0] <= box[3] ? box : null;
    }

    private static @Nullable LivingEntity entity(UUID id, String species, boolean kid) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        Entity real = minecraft.level.getEntity(id);
        if (real instanceof LivingEntity living) {
            return living;
        }
        String key = species + (kid ? "/kid" : "");
        LivingEntity standIn = STAND_INS.get(key);
        if (standIn != null && standIn.level() == minecraft.level) {
            return standIn;
        }
        Identifier typeId = Identifier.tryParse(species);
        if (typeId == null) {
            return null;
        }
        Entity created = BuiltInRegistries.ENTITY_TYPE.getOptional(typeId)
                .map(type -> type.create(minecraft.level, EntitySpawnReason.LOAD))
                .orElse(null);
        if (!(created instanceof LivingEntity living)) {
            return null;
        }
        if (kid && living instanceof AgeableMob ageable) {
            ageable.setBaby(true);
        }
        STAND_INS.put(key, living);
        return living;
    }
}
//?}
