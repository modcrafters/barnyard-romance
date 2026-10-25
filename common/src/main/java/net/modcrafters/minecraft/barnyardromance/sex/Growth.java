package net.modcrafters.minecraft.barnyardromance.sex;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.Tuning;

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

    public static int child(int mother, int father, double improvementRandom, double nutrition) {
        Tuning tuning = BarnyardConfig.current().tuning();
        double average = (percent(mother) + percent(father)) / 2.0;
        double chance = tuning.improveBaseChance() + tuning.improveNutritionChance() * Math.clamp(nutrition, 0.0, 1.0);
        double next = improvementRandom < chance ? average + tuning.improveStep() : average;
        return fromPercent(next);
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
