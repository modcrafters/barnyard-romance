package net.modcrafters.minecraft.barnyardromance.look;

import net.modcrafters.minecraft.barnyardromance.age.AgeSettings;

public final class HornGrowth {
    // Horns show up at this share of full size when the animal turns young, so they visibly appear.
    private static final double START = 0.2;

    private HornGrowth() {
    }

    // The share of full horn size: none for babies, growing through the young stretch (see Aging), then full.
    public static double size(boolean baby, long bornTick, int growthTicks, long now, AgeSettings age) {
        if (baby) {
            return 0.0;
        }
        if (bornTick < 0L || age.youngTicks() <= 0L) {
            return 1.0;
        }
        long grownAt = bornTick + Math.max(0, growthTicks);
        double t = Math.clamp((now - grownAt) / (double) age.youngTicks(), 0.0, 1.0);
        return START + (1.0 - START) * t;
    }
}
