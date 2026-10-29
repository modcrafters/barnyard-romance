package net.modcrafters.minecraft.barnyardromance.sex;

// How hard a wolf bites and how much health it has, as multipliers of vanilla's, from where its size sits between
// the smallest female and the largest male the config allows. Halfway is a vanilla wolf; the smallest female bites
// like a stick (1 damage) with half the health, the largest male like a netherite sword (8) with half again as much.
// Damage taken is not changed: a bigger wolf lasts longer because it has more health.
public final class WolfStrength {
    private static final double SMALLEST_ATTACK = 0.25;
    private static final double LARGEST_ATTACK = 2.0;
    private static final double SMALLEST_HEALTH = 0.5;
    private static final double LARGEST_HEALTH = 1.5;

    private WolfStrength() {
    }

    public static double attack(double scale, SizeSettings size) {
        return between(SMALLEST_ATTACK, LARGEST_ATTACK, position(scale, size));
    }

    public static double health(double scale, SizeSettings size) {
        return between(SMALLEST_HEALTH, LARGEST_HEALTH, position(scale, size));
    }

    // 0 for the smallest female, 1 for the largest male, 0.5 when every wolf is the same size.
    private static double position(double scale, SizeSettings size) {
        double span = size.largest() - size.smallest();
        if (span <= 0.0) {
            return 0.5;
        }
        return Math.clamp((scale - size.smallest()) / span, 0.0, 1.0);
    }

    // A straight line from the smallest to vanilla, then another from vanilla to the largest, so halfway is always 1.
    private static double between(double smallest, double largest, double position) {
        if (position < 0.5) {
            return smallest + (1.0 - smallest) * position * 2.0;
        }
        return 1.0 + (largest - 1.0) * (position * 2.0 - 1.0);
    }
}
