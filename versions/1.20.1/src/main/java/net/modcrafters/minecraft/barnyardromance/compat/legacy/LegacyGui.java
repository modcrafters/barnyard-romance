package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

// GuiGraphics.blitSprite for Minecraft 1.20.1, which has no GUI sprite atlas (1.20.2). Each of the mod's sprites is
// drawn whole from its own file, textures/gui/sprites/<path>.png, the same file the newer versions stitch into the
// atlas. Vanilla's HUD sprites only exist inside icons.png here, so those few are cut from it. Stonecutter writes these
// calls in place of graphics.blitSprite on 1.20.1. Client only.
public final class LegacyGui {
    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");
    // Where vanilla's HUD sprites sit in icons.png (256 x 256), as Gui draws them.
    private static final Map<String, int[]> HUD = Map.of(
            "hud/heart/container", new int[]{16, 0},
            "hud/heart/full", new int[]{52, 0},
            "hud/heart/half", new int[]{61, 0},
            "hud/food_empty", new int[]{16, 27},
            "hud/food_full", new int[]{52, 27},
            "hud/food_half", new int[]{61, 27}
    );

    private LegacyGui() {
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        RenderSystem.enableBlend();
        int[] hud = hudIcon(sprite);
        if (hud != null) {
            graphics.blit(ICONS, x, y, width, height, hud[0], hud[1], 9, 9, 256, 256);
        } else {
            graphics.blit(texture(sprite), x, y, width, height, 0.0F, 0.0F, 1, 1, 1, 1);
        }
        RenderSystem.disableBlend();
    }

    // Part of a sprite: the sprite is spriteWidth x spriteHeight, and u, v, width and height pick the part, in pixels.
    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int spriteWidth, int spriteHeight, int u, int v,
                                  int x, int y, int width, int height) {
        RenderSystem.enableBlend();
        graphics.blit(texture(sprite), x, y, u, v, width, height, spriteWidth, spriteHeight);
        RenderSystem.disableBlend();
    }

    static ResourceLocation texture(ResourceLocation sprite) {
        return new ResourceLocation(sprite.getNamespace(), "textures/gui/sprites/" + sprite.getPath() + ".png");
    }

    private static int[] hudIcon(ResourceLocation sprite) {
        return ResourceLocation.DEFAULT_NAMESPACE.equals(sprite.getNamespace()) ? HUD.get(sprite.getPath()) : null;
    }
}
