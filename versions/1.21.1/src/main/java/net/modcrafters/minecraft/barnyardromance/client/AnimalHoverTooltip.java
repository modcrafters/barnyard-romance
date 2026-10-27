package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.Optional;

// The 1.21.1 copy of src/.../client/AnimalHoverTooltip. The camera can't project a point here, so this
// rebuilds the world projection the way GameRenderer does.
public final class AnimalHoverTooltip {
    private AnimalHoverTooltip() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null || minecraft.options.hideGui) {
            return;
        }
        if (!(minecraft.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Animal animal)) {
            return;
        }
        Optional<AnimalTraits> traits = Services.DATA.get(animal);
        if (traits.isEmpty()) {
            return;
        }
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        Vec3 feet = animal.getPosition(partialTick);
        Vec3 aboveHead = new Vec3(feet.x, feet.y + animal.getBbHeight() + 0.3, feet.z);
        Optional<ScreenPoint> anchor = project(minecraft, aboveHead, graphics.guiWidth(), graphics.guiHeight());
        if (anchor.isEmpty()) {
            return;
        }
        HerdCard.draw(
                graphics,
                minecraft.font,
                animal,
                traits.get(),
                animal.level().getGameTime(),
                BarnyardConfig.current().sizeFor(AnimalSex.speciesId(animal)),
                anchor.get(),
                graphics.guiWidth(),
                graphics.guiHeight()
        );
    }

    // The FOV setting times the player's FOV modifier (sprinting, the spyglass), then the camera's rotation.
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
        Matrix4f viewRotation = new Matrix4f().rotation(camera.rotation().conjugate(new Quaternionf()));
        minecraft.gameRenderer.getProjectionMatrix(fov).mul(viewRotation).transform(clip);
        return ClipProjection.toScreen(clip.x, clip.y, clip.w, width, height);
    }
}
