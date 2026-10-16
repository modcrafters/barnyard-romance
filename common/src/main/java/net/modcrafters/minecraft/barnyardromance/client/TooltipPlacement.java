package net.modcrafters.minecraft.barnyardromance.client;

public final class TooltipPlacement {
    private static final int MARGIN = 4;
    private static final int GAP = 6;

    private TooltipPlacement() {
    }

    public static ScreenPoint place(int anchorX, int anchorY, int tooltipWidth, int tooltipHeight, int screenWidth, int screenHeight) {
        return new ScreenPoint(
                clamp(anchorX - tooltipWidth / 2, tooltipWidth, screenWidth),
                clamp(anchorY - tooltipHeight - GAP, tooltipHeight, screenHeight)
        );
    }

    private static int clamp(int value, int size, int screenSize) {
        int max = screenSize - size - MARGIN;
        if (max < MARGIN) {
            return MARGIN;
        }
        return Math.clamp(value, MARGIN, max);
    }
}
