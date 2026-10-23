package net.modcrafters.minecraft.barnyardromance.client;

public final class InspectorLayout {
    public static final int WIDTH = 320;
    public static final int HEIGHT = 152;
    public static final int BORDER = 4;
    public static final int TITLE_X = 8;
    public static final int TITLE_Y = 6;
    public static final int PORTRAIT_X = 8;
    public static final int PORTRAIT_Y = 18;
    public static final int PORTRAIT = 84;
    public static final int STATS_X = 102;
    public static final int HEADER_Y = 20;
    public static final int ROW_FAMILIAR = 38;
    public static final int ROW_FEED = 54;
    public static final int ROW_GROWTH = 70;
    public static final int ROW_HEALTH = 96;
    public static final int ROW_BREEDING = 122;
    public static final int DETAIL_OFFSET = 12;
    public static final int ICON = 13;
    public static final int LABEL_W = 50;
    public static final int BAR_W = 96;
    public static final int MARKER_W = 6;

    private InspectorLayout() {
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
