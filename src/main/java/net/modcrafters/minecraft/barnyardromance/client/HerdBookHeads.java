package net.modcrafters.minecraft.barnyardromance.client;

// 26.3 draws entities in screens from render states. On 1.21.1 the book goes without head close-ups, as designed.
//? if >=26.1 {

import com.mojang.blaze3d.vertex.PoseStack;
import net.modcrafters.minecraft.barnyardromance.look.AnimalLookState;
import net.modcrafters.minecraft.barnyardromance.look.HeadBox;
import net.modcrafters.minecraft.barnyardromance.look.HeadShapes;
import net.modcrafters.minecraft.barnyardromance.look.ModelParts;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

// Draws a close-up of an animal's head into a small square of the herd book, from its own model: a loaded animal
// as it looks now, anything else as a stand-in of its species. The head part (with its ears, horns and snout) is
// framed: the first of the barnyard_heads paths the model has, so Fresh Animations' head (body/EMF_body/EMF_head2) is
// found before vanilla's "head", or else every part named "head", so modded animals work as long as their model names
// it that.
public final class HerdBookHeads {
    public static final boolean AVAILABLE = true;
    // Turned a little, so the face doesn't look flat.
    private static final float TURN = 25.0F;
    private static final float FILL = 0.95F;
    private static final Map<String, LivingEntity> STAND_INS = new HashMap<>();
    private static int lastStandInId;
    // Per animal: where the renderer places its model, so it only has to be asked once, and where its head is.
    private static final Map<LivingEntity, Frame> FRAMES = new WeakHashMap<>();

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
        Frame frame = FRAMES.computeIfAbsent(entity, key -> frame(renderer, living, species));
        // The model pass measures the head again right after drawing this portrait (measureDrawn), for the next frame.
        ((AnimalLookState) living).barnyardromance$setPortrait(frame.measure());
        float[] corners = frame.measure().corners;
        float[] box = frame.placed() == null ? null : HeadBox.box(corners != null ? corners : frame.firstCorners(), frame.placed());
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

    // Where the head was when this animal's portrait was last drawn: the corners of its cubes in model space, as x, y, z
    // triples (empty when the model has none of the head parts), or null before the first draw.
    public static final class Measure {
        private final List<List<String>> candidates;
        private volatile float[] corners;

        private Measure(List<List<String>> candidates) {
            this.candidates = candidates;
        }
    }

    // placed is null when the renderer handed over no model.
    private record Frame(@Nullable Matrix4f placed, float[] firstCorners, Measure measure) {
    }

    // Called by the model pass right after it drew a portrait's model, with the pose that model was drawn with.
    // Animation mods such as Entity Model Features pose a model only while drawing it, so this is where the head is.
    public static void measureDrawn(Object portrait, Model<?> model) {
        if (portrait instanceof Measure measure) {
            measure.corners = corners(model, measure.candidates);
        }
    }

    // Lets the renderer pick its model (variant, baby) and place it, and catches the first model it hands over. Until
    // the portrait is first drawn, the head is measured from the model's own pose.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Frame frame(EntityRenderer renderer, LivingEntityRenderState state, String species) {
        Measure measure = new Measure(HeadShapes.candidates(species));
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
            // Later layers may not like the recorder; the body model is already caught by then.
        }
        if (model[0] == null) {
            return new Frame(null, new float[0], measure);
        }
        model[0].setupAnim(state);
        float[] corners = corners(model[0], measure.candidates);
        model[0].resetPose();
        return new Frame(placed[0], corners, measure);
    }

    // The corners of the cubes under the first head part the model has, in model space, as the model is posed now.
    // Without any of them, every part named "head" counts, wherever it is (a horse's is head_parts/head).
    private static float[] corners(Model<?> model, List<List<String>> candidates) {
        List<String> head = null;
        for (List<String> candidate : candidates) {
            if (ModelParts.find(model, candidate) != null) {
                head = candidate;
                break;
            }
        }
        String prefix = head == null ? null : "/" + String.join("/", head) + "/";
        List<Float> found = new ArrayList<>();
        model.root().visit(new PoseStack(), (cubePose, partPath, index, cube) -> {
            String path = partPath + "/";
            if (prefix != null ? !path.startsWith(prefix) : !path.contains("/head/")) {
                return;
            }
            for (int corner = 0; corner < 8; corner++) {
                Vector3f point = cubePose.pose().transformPosition(new Vector3f(
                        ((corner & 1) == 0 ? cube.minX : cube.maxX) / 16.0F,
                        ((corner & 2) == 0 ? cube.minY : cube.maxY) / 16.0F,
                        ((corner & 4) == 0 ? cube.minZ : cube.maxZ) / 16.0F));
                found.add(point.x);
                found.add(point.y);
                found.add(point.z);
            }
        });
        float[] corners = new float[found.size()];
        for (int i = 0; i < corners.length; i++) {
            corners[i] = found.get(i);
        }
        return corners;
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
        // Never added to the level, so it gets no id of its own; renderers ask for one. Real ids are never negative.
        living.setId(--lastStandInId);
        STAND_INS.put(key, living);
        return living;
    }
}
//?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.UUID;

public final class HerdBookHeads {
    public static final boolean AVAILABLE = false;

    private HerdBookHeads() {
    }

    public static void draw(GuiGraphicsExtractor graphics, UUID id, String species, boolean kid, int x, int y, int size) {
    }
}
*///?}
