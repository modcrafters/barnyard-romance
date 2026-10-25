package net.modcrafters.minecraft.barnyardromance.sex;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;

public final class Growth {
    private Growth() {
    }

    public static int fast() {
        return BarnyardConfig.current().tuning().growthFastTicks();
    }

    public static int slow() {
        return BarnyardConfig.current().tuning().growthSlowTicks();
    }

    // Growth for an animal with no recorded parents: wild spawns and old worlds.
    public static int wild() {
        return BarnyardConfig.current().tuning().growthWildTicks();
    }

    public static double percent(int ticks) {
        int fast = fast();
        int slow = slow();
        if (slow <= fast) {
            return 1.0;
        }
        return Math.clamp((slow - ticks) / (double) (slow - fast), 0.0, 1.0);
    }

    public static int fromPercent(double percent) {
        int fast = fast();
        int slow = slow();
        double clamped = Math.clamp(percent, 0.0, 1.0);
        return (int) Math.round(slow - clamped * (slow - fast));
    }

    public static String clock(int ticks) {
        long totalSeconds = Math.max(0, ticks) / 20L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        if (minutes > 0L) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }
}
