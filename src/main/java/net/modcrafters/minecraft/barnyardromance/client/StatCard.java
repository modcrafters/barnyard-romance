package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.stats.StatKind;
import net.modcrafters.minecraft.barnyardromance.stats.StatLine;
import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=1.21.6
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;

import java.util.ArrayList;
import java.util.List;

// The short card grown into the whole stat sheet: the sneak view. One row per line, with its icon, its words and,
// for the kinds that have one, a bar.
public final class StatCard {
    private static final int NOTE = 0xFFC9B38A;

    private StatCard() {
    }

    private record Row(StatKind kind, String text, double fill, boolean bar, int width) {
    }

    public static void draw(
            GuiGraphicsExtractor graphics,
            Font font,
            Animal animal,
            AnimalTraits traits,
            List<StatLine> lines,
            SizeSettings size,
            ScreenPoint anchor,
            int screenWidth,
            int screenHeight
    ) {
        String sign = traits.sex() == Sex.FEMALE ? "\u2640" : "\u2642";
        String letter = SizeClass.of(traits.sex(), traits.scale(), size).name();
        String name = fit(font, animal.getName().getString(), StatCardLayout.nameBudget(font.width(sign), font.width(letter)));
        int titleWidth = font.width(sign) + StatCardLayout.GAP + font.width(letter) + StatCardLayout.TITLE_GAP + font.width(name);
        String subtitle = null;
        List<Row> rows = new ArrayList<>();
        for (StatLine line : lines) {
            String words = TextComponents.of(line.text()).getString();
            if (line.kind() == StatKind.SIZE) {
                subtitle = fit(font, words, StatCardLayout.MAX_WIDTH - StatCardLayout.PAD * 2);
                continue;
            }
            boolean bar = StatIcons.hasBar(line.kind());
            String shown = fit(font, words, StatCardLayout.textBudget(bar));
            int width = StatCardLayout.ICON + StatCardLayout.GAP + font.width(shown) + (bar ? StatCardLayout.GAP + StatCardLayout.BAR : 0);
            rows.add(new Row(line.kind(), shown, line.fill(), bar, width));
        }
        List<Integer> widths = new ArrayList<>();
        for (Row row : rows) {
            widths.add(row.width());
        }
        if (subtitle != null) {
            widths.add(font.width(subtitle));
        }
        int cardWidth = StatCardLayout.width(titleWidth, widths);
        int cardHeight = StatCardLayout.height(rows.size() + (subtitle == null ? 0 : 1));
        ScreenPoint origin = TooltipPlacement.place(anchor.x(), anchor.y(), cardWidth, cardHeight, screenWidth, screenHeight);

        int x = origin.x();
        int y = origin.y();
        HerdCard.frame(graphics, x, y, cardWidth, cardHeight);
        int left = x + StatCardLayout.PAD;
        int right = x + cardWidth - StatCardLayout.PAD;
        int rowY = y + StatCardLayout.PAD;
        graphics.text(font, sign, left, rowY, traits.sex() == Sex.FEMALE ? HerdCard.FEMALE : HerdCard.MALE);
        int letterX = left + font.width(sign) + 3;
        graphics.text(font, letter, letterX, rowY, HerdCard.NAME);
        graphics.text(font, name, letterX + font.width(letter) + 4, rowY, HerdCard.WHITE);
        rowY += StatCardLayout.TITLE;
        if (subtitle != null) {
            graphics.text(font, subtitle, left, rowY, HerdCard.NAME);
            rowY += StatCardLayout.LINE;
        }
        for (Row row : rows) {
            Identifier icon = StatIcons.of(row.kind());
            if (icon != null) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, left, rowY - 1, StatCardLayout.ICON, StatCardLayout.ICON);
            }
            int color = row.kind() == StatKind.NOTE ? NOTE : HerdCard.WHITE;
            graphics.text(font, row.text(), left + StatCardLayout.ICON + StatCardLayout.GAP, rowY, color);
            if (row.bar()) {
                HerdCard.bar(graphics, right - StatCardLayout.BAR, rowY + 1, StatCardLayout.BAR, 6, row.fill(), StatIcons.barColor(row.kind()));
            }
            rowY += StatCardLayout.LINE;
        }
    }

    private static String fit(Font font, String text, int width) {
        if (font.width(text) <= width) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(0, width - font.width("\u2026"))) + "\u2026";
    }
}
