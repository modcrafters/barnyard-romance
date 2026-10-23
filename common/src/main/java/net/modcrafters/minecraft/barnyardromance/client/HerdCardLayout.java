package net.modcrafters.minecraft.barnyardromance.client;

public final class HerdCardLayout {
    public static final int PAD = 4;
    public static final int PORTRAIT = 32;
    public static final int TEXT_GAP = 6;
    public static final int BAR_WIDTH = 46;
    public static final int HEART_STEP = 8;
    public static final int HEARTS_PER_ROW = 10;

    private HerdCardLayout() {
    }

    public static int hearts(float maxHealth) {
        return Math.max(1, (int) Math.ceil(maxHealth / 2.0f));
    }

    public static int heartRows(int heartCount) {
        return Math.max(1, (heartCount + HEARTS_PER_ROW - 1) / HEARTS_PER_ROW);
    }

    public static int width(int titleWidth, int barRowWidth, int heartRowWidth) {
        int text = Math.max(titleWidth, Math.max(barRowWidth, heartRowWidth));
        return PAD + PORTRAIT + TEXT_GAP + text + PAD;
    }

    public static int height(int heartRows) {
        int text = 10 + 4 + 9 + 3 + 9;
        return PAD + Math.max(PORTRAIT, text) + 4 + heartRows * 10 + PAD;
    }

    public static int textX(int originX) {
        return originX + PAD + PORTRAIT + TEXT_GAP;
    }
}
