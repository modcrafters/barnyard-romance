package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.ScaleRoll;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;

public final class HerdCard {
    private static final int GOLD = 0xFFE0B15A;
    private static final int GOLD_LIGHT = 0xFFFFE3A1;
    private static final int GOLD_DARK = 0xFF8A5A22;
    private static final int WOOD = 0xFF3C2415;
    private static final int GRAIN = 0xFF2A180E;
    private static final int TRACK = 0xFF140E09;
    private static final int NAME = 0xFFFFE08A;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int FEMALE = 0xFFF27BB0;
    private static final int MALE = 0xFF7EB6F2;
    private static final int FAMILIAR = 0xFFE07A6A;
    private static final int FEED = 0xFF8FCB6B;
    private static final Identifier HEART_CONTAINER = Identifier.withDefaultNamespace("hud/heart/container");
    private static final Identifier HEART_FULL = Identifier.withDefaultNamespace("hud/heart/full");
    private static final Identifier HEART_HALF = Identifier.withDefaultNamespace("hud/heart/half");
    private static final String FEMALE_MARK = "\u2640";
    private static final String MALE_MARK = "\u2642";
    private static final String[] TRUST_HEART = {
            ".##.##.",
            "#######",
            ".#####.",
            "..###..",
            "...#..."
    };
    private static final String[] FEED_MARK = {
            "#..#..#",
            ".#.#.#.",
            "..#.#..",
            "...#...",
            "...#...",
            "..###.."
    };

    private HerdCard() {
    }

    public static void draw(
            GuiGraphicsExtractor graphics,
            Font font,
            Animal animal,
            AnimalTraits traits,
            long gameTime,
            SizeSettings size,
            ScreenPoint anchor,
            int screenWidth,
            int screenHeight
    ) {
        String sign = traits.sex() == Sex.FEMALE ? FEMALE_MARK : MALE_MARK;
        String letter = SizeClass.of(traits.sex(), traits.scale(), size).name();
        String name = animal.getName().getString();
        int titleWidth = font.width(sign) + 3 + font.width(letter) + 4 + font.width(name);
        int barRow = 8 + 3 + HerdCardLayout.BAR_WIDTH;
        int heartCount = HerdCardLayout.hearts(animal.getMaxHealth());
        int heartsOnRow = Math.min(heartCount, HerdCardLayout.HEARTS_PER_ROW);
        int heartRow = heartsOnRow * HerdCardLayout.HEART_STEP + 1;
        int rows = HerdCardLayout.heartRows(heartCount);
        int cardWidth = HerdCardLayout.width(titleWidth, barRow, heartRow);
        int cardHeight = HerdCardLayout.height(rows);
        ScreenPoint origin = TooltipPlacement.place(anchor.x(), anchor.y(), cardWidth, cardHeight, screenWidth, screenHeight);
        paint(graphics, font, animal, traits, gameTime, origin.x(), origin.y(), cardWidth, cardHeight, sign, letter, name);
    }

    private static void paint(
            GuiGraphicsExtractor graphics,
            Font font,
            Animal animal,
            AnimalTraits traits,
            long gameTime,
            int x,
            int y,
            int width,
            int height,
            String sign,
            String letter,
            String name
    ) {
        frame(graphics, x, y, width, height);
        portrait(graphics, animal, x + HerdCardLayout.PAD, y + HerdCardLayout.PAD);
        int textX = HerdCardLayout.textX(x);
        int textY = y + HerdCardLayout.PAD;
        int signColor = traits.sex() == Sex.FEMALE ? FEMALE : MALE;
        graphics.text(font, sign, textX, textY, signColor);
        int letterX = textX + font.width(sign) + 3;
        graphics.text(font, letter, letterX, textY, NAME);
        graphics.text(font, name, letterX + font.width(letter) + 4, textY, WHITE);
        int rowY = textY + 12;
        mark(graphics, textX, rowY + 1, TRUST_HEART, FAMILIAR);
        bar(graphics, textX + 11, rowY + 2, HerdCardLayout.BAR_WIDTH, 6, traits.familiarity(), FAMILIAR);
        rowY += 12;
        mark(graphics, textX, rowY, FEED_MARK, FEED);
        bar(graphics, textX + 11, rowY + 1, HerdCardLayout.BAR_WIDTH, 6, Familiarity.feedReady(traits.lastFedTick(), gameTime), FEED);
        int heartsY = y + height - HerdCardLayout.PAD - HerdCardLayout.heartRows(HerdCardLayout.hearts(animal.getMaxHealth())) * 10;
        hearts(graphics, x + HerdCardLayout.PAD, heartsY, animal.getHealth(), animal.getMaxHealth());
    }

    private static void hearts(GuiGraphicsExtractor graphics, int x, int y, float health, float maxHealth) {
        int count = HerdCardLayout.hearts(maxHealth);
        for (int i = 0; i < count; i++) {
            int hx = x + (i % HerdCardLayout.HEARTS_PER_ROW) * HerdCardLayout.HEART_STEP;
            int hy = y + (i / HerdCardLayout.HEARTS_PER_ROW) * 10;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_CONTAINER, hx, hy, 9, 9);
            float remaining = health - i * 2.0F;
            if (remaining >= 2.0F) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_FULL, hx, hy, 9, 9);
            } else if (remaining >= 1.0F) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_HALF, hx, hy, 9, 9);
            }
        }
    }

    private static void frame(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, GOLD_DARK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, GOLD);
        graphics.fill(x + 2, y + 2, x + width - 2, y + 3, GOLD_LIGHT);
        graphics.fill(x + 2, y + 2, x + 3, y + height - 2, GOLD_LIGHT);
        graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, WOOD);
        for (int row = y + 6; row < y + height - 4; row += 4) {
            graphics.fill(x + 4, row, x + width - 4, row + 1, GRAIN);
        }
    }

    private static void portrait(GuiGraphicsExtractor graphics, Animal animal, int x, int y) {
        graphics.fill(x, y, x + HerdCardLayout.PORTRAIT, y + HerdCardLayout.PORTRAIT, GOLD_DARK);
        graphics.fill(x + 1, y + 1, x + HerdCardLayout.PORTRAIT - 1, y + HerdCardLayout.PORTRAIT - 1, GOLD);
        int inner = 2;
        int x0 = x + inner;
        int y0 = y + inner;
        int x1 = x + HerdCardLayout.PORTRAIT - inner;
        int y1 = y + HerdCardLayout.PORTRAIT - inner;
        graphics.fill(x0, y0, x1, y1, 0xFF1A100A);
        float body = Math.max(animal.getBbHeight(), 0.5F);
        int entityScale = Math.max(8, Math.round((y1 - y0 - 2) / body));
        graphics.enableScissor(x0, y0, x1, y1);
        try {
            InventoryScreen.extractEntityInInventoryFollowsMouse(
                    graphics,
                    x0,
                    y0,
                    x1,
                    y1,
                    entityScale,
                    0.0625F,
                    (x0 + x1) / 2.0F,
                    (y0 + y1) / 2.0F,
                    animal
            );
        } catch (RuntimeException ignored) {
            // A modded renderer that cannot draw in a GUI leaves the empty frame.
        }
        graphics.disableScissor();
    }

    private static void bar(GuiGraphicsExtractor graphics, int x, int y, int width, int height, double fill, int color) {
        graphics.fill(x, y, x + width, y + height, TRACK);
        int filled = (int) Math.round(Math.clamp(fill, 0.0, 1.0) * (width - 2));
        if (filled > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + filled, y + height - 1, color);
        }
    }

    private static void mark(GuiGraphicsExtractor graphics, int x, int y, String[] rows, int color) {
        for (int row = 0; row < rows.length; row++) {
            String line = rows[row];
            for (int column = 0; column < line.length(); column++) {
                if (line.charAt(column) == '#') {
                    graphics.fill(x + column, y + row, x + column + 1, y + row + 1, color);
                }
            }
        }
    }
}
