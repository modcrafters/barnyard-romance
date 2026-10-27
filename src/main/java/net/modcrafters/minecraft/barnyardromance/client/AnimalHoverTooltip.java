package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Optional;

// versions/1.21.1 has its own copy: the camera can't project a point there.
public final class AnimalHoverTooltip {
    private AnimalHoverTooltip() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.gui.screen() != null || minecraft.gui.hud.isHidden()) {
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
        Optional<ScreenPoint> anchor = project(minecraft.gameRenderer.mainCamera(), aboveHead, graphics.guiWidth(), graphics.guiHeight());
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

    private static Optional<ScreenPoint> project(Camera camera, Vec3 world, int width, int height) {
        Vec3 cameraPosition = camera.position();
        Vector4f clip = new Vector4f(
                (float) (world.x - cameraPosition.x),
                (float) (world.y - cameraPosition.y),
                (float) (world.z - cameraPosition.z),
                1.0f
        );
        camera.getViewRotationProjectionMatrix(new Matrix4f()).transform(clip);
        return ClipProjection.toScreen(clip.x, clip.y, clip.w, width, height);
    }
}
