package net.modcrafters.minecraft.barnyardromance.nest;

// Where things sit in the nesting box window: sixteen nests in four rows of four, each with room for its countdown
// underneath, above the player's inventory. Positions are the top-left of a slot's item, as Minecraft's slots use.
public final class NestingBoxLayout {
    public static final int WIDTH = 176;
    public static final int HEIGHT = 232;
    public static final int NESTS_PER_ROW = 4;
    public static final int CELL = 30;
    public static final int FIRST_NEST_X = (WIDTH - (NESTS_PER_ROW - 1) * CELL - 16) / 2;
    public static final int FIRST_NEST_Y = 20;
    public static final int ROW_STEP = 30;
    // The inventory sits at the bottom, as a chest's does.
    public static final int INVENTORY_Y = HEIGHT - 82;
    public static final int HOTBAR_Y = HEIGHT - 24;
    public static final int INVENTORY_LABEL_Y = HEIGHT - 94;

    private NestingBoxLayout() {
    }

    public static int nestX(int slot) {
        return FIRST_NEST_X + (slot % NESTS_PER_ROW) * CELL;
    }

    public static int nestY(int slot) {
        return FIRST_NEST_Y + (slot / NESTS_PER_ROW) * ROW_STEP;
    }

    // The countdown under a nest, in real minutes and seconds.
    public static String timer(long ticks) {
        long seconds = (Math.max(0L, ticks) + 19L) / 20L;
        return String.format(java.util.Locale.ROOT, "%d:%02d", seconds / 60L, seconds % 60L);
    }
}
