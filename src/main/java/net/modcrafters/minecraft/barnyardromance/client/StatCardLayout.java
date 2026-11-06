package net.modcrafters.minecraft.barnyardromance.client;

import java.util.List;

public final class StatCardLayout {
    public static final int MAX_WIDTH = 180;
    public static final int LINE = 10;
    public static final int PAD = 4;
    public static final int ICON = 9;
    public static final int BAR = 30;
    public static final int GAP = 3;
    // The title row: sign, size letter and name.
    public static final int TITLE = 12;
    public static final int TITLE_GAP = 4;

    private StatCardLayout() {
    }

    // lineWidths are whole rows, icon and bar included.
    public static int width(int titleWidth, List<Integer> lineWidths) {
        int widest = titleWidth;
        for (int row : lineWidths) {
            widest = Math.max(widest, row);
        }
        return Math.min(MAX_WIDTH, PAD * 2 + widest);
    }

    // lineCount counts every row under the title, the subtitle too.
    public static int height(int lineCount) {
        return PAD * 2 + TITLE + lineCount * LINE;
    }

    // The room left for the name in the title row: what the widest card has, less the sign and the size letter.
    public static int nameBudget(int signWidth, int letterWidth) {
        return Math.max(0, MAX_WIDTH - PAD * 2 - signWidth - GAP - letterWidth - TITLE_GAP);
    }

    // The most a row's text can take on a card of the widest size.
    public static int textBudget(boolean withBar) {
        return MAX_WIDTH - PAD * 2 - ICON - GAP - (withBar ? GAP + BAR : 0);
    }
}
