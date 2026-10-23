package net.modcrafters.minecraft.barnyardromance.sex;

public final class Growth {
    public static final int FAST = 15_000;
    public static final int SLOW = 30_000;
    public static final int DEFAULT = 24_000;

    private Growth() {
    }

    public static double percent(int ticks) {
        return Math.clamp((SLOW - ticks) / (double) (SLOW - FAST), 0.0, 1.0);
    }

    public static int fromPercent(double percent) {
        double clamped = Math.clamp(percent, 0.0, 1.0);
        return (int) Math.round(SLOW - clamped * (SLOW - FAST));
    }

    public static int child(int mother, int father, double improvementRandom, double nutrition) {
        double average = (percent(mother) + percent(father)) / 2.0;
        double chance = OffspringScale.BASE_IMPROVEMENT_CHANCE + OffspringScale.NUTRITION_CHANCE * Math.clamp(nutrition, 0.0, 1.0);
        double next = improvementRandom < chance ? average + OffspringScale.IMPROVEMENT : average;
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
