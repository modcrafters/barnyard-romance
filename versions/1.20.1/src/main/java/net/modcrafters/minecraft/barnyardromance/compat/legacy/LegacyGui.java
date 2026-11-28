package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// GuiGraphics.blitSprite for Minecraft 1.20.1, which has no GUI sprite atlas (1.20.2). Each of the mod's sprites is
// drawn from its own file, textures/gui/sprites/<path>.png, the same file the newer versions stitch into the atlas, and
// nine-sliced when its .mcmeta says so, as the atlas would. Vanilla's HUD sprites only exist inside icons.png here, so
// those few are cut from it. Stonecutter writes these calls in place of graphics.blitSprite on 1.20.1. Client only.
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
    // The "gui" section of a sprite's .mcmeta, which the newer versions' atlas reads. Only nine_slice is drawn
    // differently; any other scaling stretches, as a sprite without the section does.
    private static final MetadataSectionSerializer<Optional<NineSlice>> GUI_SECTION = new MetadataSectionSerializer<>() {
        @Override
        public String getMetadataSectionName() {
            return "gui";
        }

        @Override
        public Optional<NineSlice> fromJson(JsonObject json) {
            JsonObject scaling = GsonHelper.getAsJsonObject(json, "scaling", new JsonObject());
            if (!"nine_slice".equals(GsonHelper.getAsString(scaling, "type", "stretch"))) {
                return Optional.empty();
            }
            JsonElement border = scaling.get("border");
            int[] sides = border.isJsonPrimitive()
                    ? new int[]{border.getAsInt(), border.getAsInt(), border.getAsInt(), border.getAsInt()}
                    : new int[]{GsonHelper.getAsInt(border.getAsJsonObject(), "left"), GsonHelper.getAsInt(border.getAsJsonObject(), "top"),
                    GsonHelper.getAsInt(border.getAsJsonObject(), "right"), GsonHelper.getAsInt(border.getAsJsonObject(), "bottom")};
            return Optional.of(new NineSlice(GsonHelper.getAsInt(scaling, "width"), GsonHelper.getAsInt(scaling, "height"),
                    sides[0], sides[1], sides[2], sides[3]));
        }
    };
    // Each sprite's scaling, read once after each resource reload.
    private static final Map<ResourceLocation, Optional<NineSlice>> SCALING = new ConcurrentHashMap<>();

    private LegacyGui() {
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        RenderSystem.enableBlend();
        int[] hud = hudIcon(sprite);
        Optional<NineSlice> nineSlice = hud == null ? nineSlice(sprite) : Optional.empty();
        if (hud != null) {
            graphics.blit(ICONS, x, y, width, height, hud[0], hud[1], 9, 9, 256, 256);
        } else if (nineSlice.isPresent()) {
            blitNineSliced(graphics, texture(sprite), nineSlice.get(), x, y, width, height);
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

    // Forgets every sprite's scaling, so the .mcmeta files are read again after a resource reload.
    public static ResourceManagerReloadListener reloadListener() {
        return resourceManager -> SCALING.clear();
    }

    static ResourceLocation texture(ResourceLocation sprite) {
        return new ResourceLocation(sprite.getNamespace(), "textures/gui/sprites/" + sprite.getPath() + ".png");
    }

    private static int[] hudIcon(ResourceLocation sprite) {
        return ResourceLocation.DEFAULT_NAMESPACE.equals(sprite.getNamespace()) ? HUD.get(sprite.getPath()) : null;
    }

    private static Optional<NineSlice> nineSlice(ResourceLocation sprite) {
        return SCALING.computeIfAbsent(sprite, key -> {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(texture(key));
            try {
                return resource.isPresent() ? resource.get().metadata().getSection(GUI_SECTION).flatMap(scaling -> scaling) : Optional.empty();
            } catch (IOException | RuntimeException e) {
                return Optional.empty();
            }
        });
    }

    // Nine-slicing as 1.20.2 and later draw it (GuiGraphics.blitNineSlicedSprite): corners as they are, edges and middle
    // tiled from their top left. 1.20.1's own blitNineSliced can't stand in: it reads a 256 x 256 texture, and tiles
    // from the middle.
    private static void blitNineSliced(GuiGraphics graphics, ResourceLocation texture, NineSlice slice, int x, int y, int width, int height) {
        int left = Math.min(slice.left(), width / 2);
        int right = Math.min(slice.right(), width / 2);
        int top = Math.min(slice.top(), height / 2);
        int bottom = Math.min(slice.bottom(), height / 2);
        int sw = slice.width();
        int sh = slice.height();
        int middleWidth = width - left - right;
        int middleHeight = height - top - bottom;
        int sourceMiddleWidth = sw - left - right;
        int sourceMiddleHeight = sh - top - bottom;
        blitTiled(graphics, texture, slice, 0, 0, left, top, x, y, left, top);
        blitTiled(graphics, texture, slice, left, 0, sourceMiddleWidth, top, x + left, y, middleWidth, top);
        blitTiled(graphics, texture, slice, sw - right, 0, right, top, x + width - right, y, right, top);
        blitTiled(graphics, texture, slice, 0, top, left, sourceMiddleHeight, x, y + top, left, middleHeight);
        blitTiled(graphics, texture, slice, left, top, sourceMiddleWidth, sourceMiddleHeight, x + left, y + top, middleWidth, middleHeight);
        blitTiled(graphics, texture, slice, sw - right, top, right, sourceMiddleHeight, x + width - right, y + top, right, middleHeight);
        blitTiled(graphics, texture, slice, 0, sh - bottom, left, bottom, x, y + height - bottom, left, bottom);
        blitTiled(graphics, texture, slice, left, sh - bottom, sourceMiddleWidth, bottom, x + left, y + height - bottom, middleWidth, bottom);
        blitTiled(graphics, texture, slice, sw - right, sh - bottom, right, bottom, x + width - right, y + height - bottom, right, bottom);
    }

    // Fills width x height at x, y with the tileWidth x tileHeight part of the sprite at u, v, cutting the last tiles short.
    private static void blitTiled(GuiGraphics graphics, ResourceLocation texture, NineSlice slice, int u, int v, int tileWidth, int tileHeight,
                                  int x, int y, int width, int height) {
        if (width <= 0 || height <= 0 || tileWidth <= 0 || tileHeight <= 0) {
            return;
        }
        for (int i = 0; i < width; i += tileWidth) {
            for (int j = 0; j < height; j += tileHeight) {
                graphics.blit(texture, x + i, y + j, u, v, Math.min(tileWidth, width - i), Math.min(tileHeight, height - j),
                        slice.width(), slice.height());
            }
        }
    }

    // A nine_slice scaling: the sprite's size in GUI pixels and its border's sides.
    private record NineSlice(int width, int height, int left, int top, int right, int bottom) {
    }
}
