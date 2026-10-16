package net.modcrafters.minecraft.barnyardromance.client;

public final class HerdCardLayout {
    public static final int PAD = 6;
    public static final int PORTRAIT = 44;
    public static final int TEXT_GAP = 8;
    public static final int BAR_WIDTH = 54;

    private HerdCardLayout() {
    }

    public static int width(int nameWidth, int sizeRowWidth, int pregnancyRowWidth, boolean pregnant) {
        int text = Math.max(nameWidth, sizeRowWidth);
        if (pregnant) {
            text = Math.max(text, pregnancyRowWidth);
        }
        return PAD + PORTRAIT + TEXT_GAP + text + PAD;
    }

    public static int height(boolean pregnant) {
        int text = 2 + 11 + 13 + 14 + (pregnant ? 18 : 0);
        return PAD + Math.max(PORTRAIT, text) + PAD;
    }

    public static int textX(int originX) {
        return originX + PAD + PORTRAIT + TEXT_GAP;
    }
}
