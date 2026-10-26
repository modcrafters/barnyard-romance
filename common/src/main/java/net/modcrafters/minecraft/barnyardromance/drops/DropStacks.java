package net.modcrafters.minecraft.barnyardromance.drops;

import java.util.ArrayList;
import java.util.List;

// A sized drop can outgrow one stack; it lands as several, each no bigger than the item's own max stack size.
public final class DropStacks {
    private DropStacks() {
    }

    public static List<Integer> sizes(int count, int maxStackSize) {
        int max = Math.max(1, maxStackSize);
        List<Integer> sizes = new ArrayList<>();
        for (int left = count; left > 0; left -= max) {
            sizes.add(Math.min(left, max));
        }
        return sizes;
    }
}
