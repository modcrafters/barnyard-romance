package net.modcrafters.minecraft.barnyardromance.drops;

import java.util.Set;

public final class MeatDrop {
    private static final int VANILLA_MEAT_BEFORE_LOOTING = 3;
    private static final Set<String> MEAT = Set.of(
            "minecraft:beef",
            "minecraft:cooked_beef",
            "minecraft:porkchop",
            "minecraft:cooked_porkchop",
            "minecraft:chicken",
            "minecraft:cooked_chicken",
            "minecraft:mutton",
            "minecraft:cooked_mutton",
            "minecraft:rabbit",
            "minecraft:cooked_rabbit"
    );

    private MeatDrop() {
    }

    public static int count(double scale, double smallest, double largest, double unitRandom) {
        double span = largest - smallest;
        double t = span <= 0.0 ? 1.0 : Math.clamp((scale - smallest) / span, 0.0, 1.0);
        int min = (int) Math.round(2 + (8 - 2) * t);
        int max = (int) Math.round(3 + (9 - 3) * t);
        if (max < min) {
            int swap = min;
            min = max;
            max = swap;
        }
        if (min == max || unitRandom >= 1.0) {
            return max;
        }
        if (unitRandom <= 0.0) {
            return min;
        }
        return min + (int) Math.floor(unitRandom * (max - min + 1));
    }

    public static boolean isMeat(String itemId) {
        return MEAT.contains(itemId);
    }

    // The sized curve × the sex and age factor, then looting's extra on top, unscaled.
    public static int resizedCount(
            String itemId,
            int currentCount,
            double scale,
            double smallest,
            double largest,
            double factor,
            double curveRandom,
            double roundRandom
    ) {
        if (currentCount <= 0 || !MEAT.contains(itemId)) {
            return currentCount;
        }
        int lootingExtra = Math.max(0, currentCount - VANILLA_MEAT_BEFORE_LOOTING);
        return Yield.round(count(scale, smallest, largest, curveRandom) * factor, roundRandom) + lootingExtra;
    }
}
