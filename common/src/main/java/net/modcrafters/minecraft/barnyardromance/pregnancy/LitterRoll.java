package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.config.Range;

// Picks a value in a range. A weight of 0.5 is neutral; a heavier mother picks a bigger litter and a shorter pregnancy.
public final class LitterRoll {
    private LitterRoll() {
    }

    public static long pick(Range range, double weight, double random) {
        return range.min() + steps(range, weight, random);
    }

    public static long pickTicks(Range range, double weight, double random) {
        return range.max() - steps(range, weight, random);
    }

    private static long steps(Range range, double weight, double random) {
        double x = Math.clamp(random + (weight - 0.5) * 0.5, 0.0, 0.999999);
        return (long) Math.floor(x * (range.max() - range.min() + 1));
    }
}
