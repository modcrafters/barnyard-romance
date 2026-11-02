package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.inspect.InspectorRow;

import java.util.ArrayList;
import java.util.List;

public final class InspectorLayout {
    public static final int WIDTH = 320;
    public static final int BORDER = 4;
    public static final int TITLE_X = 8;
    public static final int TITLE_Y = 6;
    public static final int PORTRAIT_X = 8;
    public static final int PORTRAIT_Y = 18;
    public static final int PORTRAIT = 84;
    public static final int STATS_X = 102;
    public static final int HEADER_Y = 20;
    public static final int ROW_START = 38;
    public static final int BOTTOM_MARGIN = 6;
    public static final int ICON = 13;
    public static final int LABEL_W = 50;
    public static final int BAR_W = 96;
    public static final int MARKER_W = 6;

    public record Placed(InspectorRow row, int y, int height) {
    }

    private InspectorLayout() {
    }

    // Each row sits right below the one before it, so a row that is left out leaves no gap.
    public static List<Placed> stack(List<InspectorRow> rows, int heartRows) {
        List<Placed> placed = new ArrayList<>();
        int y = ROW_START;
        for (InspectorRow row : rows) {
            int height = height(row, heartRows);
            placed.add(new Placed(row, y, height));
            y += height;
        }
        return List.copyOf(placed);
    }

    public static int height(InspectorRow row, int heartRows) {
        return switch (row) {
            case GROWTH -> 26;
            case HEALTH -> 6 + 10 * Math.max(1, heartRows);
            case WILD, BREEDING, DETAIL -> 12;
            default -> 16;
        };
    }

    public static int bottom(List<Placed> placed) {
        if (placed.isEmpty()) {
            return ROW_START;
        }
        Placed last = placed.getLast();
        return last.y() + last.height();
    }

    // Tall enough for the list, and never shorter than the portrait.
    public static int panelHeight(List<Placed> placed) {
        return Math.max(PORTRAIT_Y + PORTRAIT + 1, bottom(placed)) + BOTTOM_MARGIN;
    }

    public static int barX() {
        return STATS_X + ICON + 4 + LABEL_W;
    }

    public static int valueX() {
        return barX() + BAR_W + 6;
    }

    public static int portraitScale(int box, float height, float width) {
        float tall = Math.max(height, 0.5F);
        float length = Math.max(width, 0.5F) * 1.45F;
        float span = Math.max(tall, length);
        return Math.max(8, Math.round((box - 8) / span * 0.5F));
    }

    public static int markerX(int barX, int barWidth, double fast) {
        int travel = Math.max(0, barWidth - MARKER_W);
        return barX + (int) Math.round(Math.clamp(fast, 0.0, 1.0) * travel);
    }
}
