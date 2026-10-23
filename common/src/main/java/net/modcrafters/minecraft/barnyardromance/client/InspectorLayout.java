package net.modcrafters.minecraft.barnyardromance.client;

public final class InspectorLayout {
    public static final int WIDTH = 360;
    public static final int HEIGHT = 186;
    public static final int PORTRAIT_X = 10;
    public static final int PORTRAIT_Y = 12;
    public static final int PORTRAIT = 84;
    public static final int PORTRAIT_INSET = 4;
    public static final int NAME_X = 8;
    public static final int NAME_Y = 102;
    public static final int NAME_W = 92;
    public static final int NAME_H = 40;
    public static final int STATS_X = 112;
    public static final int HEADER_Y = 16;
    public static final int ROW_FAMILIAR = 40;
    public static final int ROW_FEED = 60;
    public static final int ROW_GROWTH = 80;
    public static final int ROW_HEALTH = 108;
    public static final int ROW_PREGNANT = 130;
    public static final int ESC_Y = 166;
    public static final int ICON = 13;
    public static final int LABEL_W = 58;
    public static final int BAR_W = 104;
    public static final int MARKER_W = 6;

    private InspectorLayout() {
    }

    public static int barX() {
        return STATS_X + ICON + 5 + LABEL_W;
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
