package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.core.animal.SizeRanges;
import net.modcrafters.minecraft.barnyardromance.compat.JadeBridge;
import net.modcrafters.minecraft.barnyardromance.core.animal.Tracked;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalFacts;
import net.modcrafters.minecraft.barnyardromance.stats.LiveFacts;
import net.modcrafters.minecraft.barnyardromance.stats.StatSheet;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Optional;

// The 1.20.1 copy of src/.../client/AnimalHoverTooltip, from the 1.21.1 one: the overlay is handed the partial tick
// itself. The camera can't project a point here, so this rebuilds the world projection the way GameRenderer does.
public final class AnimalHoverTooltip {
    private AnimalHoverTooltip() {
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        if (JadeBridge.active()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null || minecraft.options.hideGui) {
            return;
        }
        if (!(minecraft.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Animal animal)) {
            return;
        }
        Optional<AnimalTraits> traits = Tracked.traits(animal);
        if (traits.isEmpty()) {
            return;
        }
        Vec3 feet = animal.getPosition(partialTick);
        Vec3 aboveHead = new Vec3(feet.x, feet.y + animal.getBbHeight() + 0.3, feet.z);
        Optional<ScreenPoint> anchor = project(minecraft, aboveHead, graphics.guiWidth(), graphics.guiHeight());
        if (anchor.isEmpty()) {
            return;
        }
        SizeSettings size = SizeRanges.rangeOf(traits.get(), AnimalSex.speciesId(animal));
        if (minecraft.options.keyShift.isDown()) {
            Optional<AnimalFacts> facts = LiveFacts.of(animal, animal.level().getGameTime(), animal.level().getDayTime());
            if (facts.isPresent()) {
                StatCard.draw(graphics, minecraft.font, animal, traits.get(), StatSheet.lines(facts.get()), size,
                        anchor.get(), graphics.guiWidth(), graphics.guiHeight());
                return;
            }
        }
        HerdCard.draw(graphics, minecraft.font, animal, traits.get(), animal.level().getGameTime(), partialTick, size,
                anchor.get(), graphics.guiWidth(), graphics.guiHeight());
    }

    // The FOV setting times the player's FOV modifier (sprinting, the spyglass), then the camera's turn (viewRotation).
    // View bobbing and the short FOV transition are left out; the card is only an anchor above the head.
    private static Optional<ScreenPoint> project(Minecraft minecraft, Vec3 world, int width, int height) {
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPosition = camera.getPosition();
        Vector4f clip = new Vector4f(
                (float) (world.x - cameraPosition.x),
                (float) (world.y - cameraPosition.y),
                (float) (world.z - cameraPosition.z),
                1.0f
        );
        double fov = minecraft.options.fov().get() * Mth.clamp(minecraft.player.getFieldOfViewModifier(), 0.1F, 1.5F);
        minecraft.gameRenderer.getProjectionMatrix(fov).mul(viewRotation(camera.getXRot(), camera.getYRot())).transform(clip);
        return ClipProjection.toScreen(clip.x, clip.y, clip.w, width, height);
    }

    // The world's turn into the camera's view, as GameRenderer.renderLevel makes it on 1.20.1: the pitch, then the yaw
    // turned half round. 1.20.1's camera rotation faces the other way (1.20.5 turned it round), so its conjugate, which
    // the newer versions' view is, would put everything in front of the camera behind it.
    static Matrix4f viewRotation(float xRot, float yRot) {
        return new Matrix4f().rotationX(xRot * Mth.DEG_TO_RAD).rotateY((yRot + 180.0F) * Mth.DEG_TO_RAD);
    }
}
