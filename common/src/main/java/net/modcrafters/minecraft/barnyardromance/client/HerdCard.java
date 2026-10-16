package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyClock;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyText;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
    private static final int PREGNANCY = 0xFFF27BB0;
    private static final Identifier HEART = Identifier.withDefaultNamespace("hud/heart/full");
    private static final String[] FEMALE_MARK = {
            "..##..",
            ".#..#.",
            ".#..#.",
            "..##..",
            "...#..",
            "..###.",
            "...#.."
    };
    private static final String[] MALE_MARK = {
            "..##..",
            ".#..#.",
            "..##.#",
            ".#...#",
            ".##...",
            ".#....",
            ".#...."
    };

    private HerdCard() {
    }

    public static void draw(
            GuiGraphicsExtractor graphics,
            Font font,
            Animal animal,
            AnimalTraits traits,
            Pregnancy pregnancy,
            long gameTime,
            SizeSettings size,
            ScreenPoint anchor,
            int screenWidth,
            int screenHeight
    ) {
        boolean pregnant = pregnancy != null;
        String name = animal.getName().getString();
        String letter = SizeClass.of(traits.sex(), traits.scale(), size).name();
        String life = animal.isBaby() ? "Baby" : "Adult";
        String time = pregnant
                ? PregnancyText.clock(PregnancyClock.remainingTicks(gameTime, pregnancy.conceivedAt(), pregnancy.durationTicks()))
                : "";
        int sizeRow = 8 + 3 + HerdCardLayout.BAR_WIDTH + 3 + 12 + 4 + font.width(letter);
        int pregnancyRow = pregnant ? 9 + 4 + HerdCardLayout.BAR_WIDTH + 4 + 16 + 2 + font.width(time) : 0;
        int cardWidth = HerdCardLayout.width(font.width(name), sizeRow, pregnancyRow, pregnant);
        int cardHeight = HerdCardLayout.height(pregnant);
        ScreenPoint origin = TooltipPlacement.place(anchor.x(), anchor.y(), cardWidth, cardHeight, screenWidth, screenHeight);
        paint(graphics, font, animal, traits, pregnancy, gameTime, size, origin.x(), origin.y(), cardWidth, cardHeight, name, letter, life, time);
    }

    private static void paint(
            GuiGraphicsExtractor graphics,
            Font font,
            Animal animal,
            AnimalTraits traits,
            Pregnancy pregnancy,
            long gameTime,
            SizeSettings size,
            int x,
            int y,
            int width,
            int height,
            String name,
            String letter,
            String life,
            String time
    ) {
        frame(graphics, x, y, width, height);
        int portraitX = x + HerdCardLayout.PAD;
        int portraitY = y + HerdCardLayout.PAD;
        portrait(graphics, animal, portraitX, portraitY);
        int textX = HerdCardLayout.textX(x);
        int textY = y + HerdCardLayout.PAD + 1;
        graphics.text(font, name, textX, textY, NAME);
        mark(graphics, textX, textY + 12, traits.sex() == Sex.FEMALE ? FEMALE_MARK : MALE_MARK, traits.sex() == Sex.FEMALE ? FEMALE : MALE);
        graphics.text(font, life, textX + 10, textY + 12, WHITE);
        int sizeY = textY + 26;
        beast(graphics, textX, sizeY + 2, 1, 0xFF5C4636);
        bar(graphics, textX + 11, sizeY + 3, HerdCardLayout.BAR_WIDTH, 6, ScaleRoll.percent(traits.sex(), traits.scale(), size), GOLD);
        beast(graphics, textX + 14 + HerdCardLayout.BAR_WIDTH, sizeY, 2, 0xFF5C4636);
        graphics.text(font, letter, textX + 30 + HerdCardLayout.BAR_WIDTH, sizeY, NAME);
        if (pregnancy == null) {
            return;
        }
        int rowY = sizeY + 16;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART, textX, rowY, 9, 9);
        double progress = PregnancyClock.progress(gameTime, pregnancy.conceivedAt(), pregnancy.durationTicks());
        bar(graphics, textX + 13, rowY + 2, HerdCardLayout.BAR_WIDTH, 6, progress, PREGNANCY);
        graphics.item(new ItemStack(Items.CLOCK), textX + 13 + HerdCardLayout.BAR_WIDTH + 4, rowY - 3);
        graphics.text(font, time, textX + 13 + HerdCardLayout.BAR_WIDTH + 22, rowY, WHITE);
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
        stud(graphics, x + 2, y + 2);
        stud(graphics, x + width - 5, y + 2);
        stud(graphics, x + 2, y + height - 5);
        stud(graphics, x + width - 5, y + height - 5);
    }

    private static void stud(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x + 1, y, x + 2, y + 3, GOLD_LIGHT);
        graphics.fill(x, y + 1, x + 3, y + 2, GOLD_LIGHT);
    }

    private static void portrait(GuiGraphicsExtractor graphics, Animal animal, int x, int y) {
        graphics.fill(x, y, x + HerdCardLayout.PORTRAIT, y + HerdCardLayout.PORTRAIT, GOLD_DARK);
        graphics.fill(x + 1, y + 1, x + HerdCardLayout.PORTRAIT - 1, y + HerdCardLayout.PORTRAIT - 1, GOLD);
        graphics.fill(x + 2, y + 2, x + HerdCardLayout.PORTRAIT - 2, y + HerdCardLayout.PORTRAIT - 2, GOLD_LIGHT);
        int inner = 4;
        int x0 = x + inner;
        int y0 = y + inner;
        int x1 = x + HerdCardLayout.PORTRAIT - inner;
        int y1 = y + HerdCardLayout.PORTRAIT - inner;
        graphics.fill(x0, y0, x1, y1, 0xFF1A100A);
        float body = Math.max(animal.getBbHeight(), 0.5F);
        int entityScale = Math.max(10, Math.round((y1 - y0 - 4) / body));
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

    private static void beast(GuiGraphicsExtractor graphics, int x, int y, int scale, int color) {
        int body = 3 + scale * 2;
        graphics.fill(x, y + 2, x + body, y + 2 + scale + 2, color);
        graphics.fill(x + body - 2, y, x + body + 1, y + 3, color);
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
