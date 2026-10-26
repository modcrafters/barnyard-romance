package net.modcrafters.minecraft.barnyardromance.drops;

import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;

// How much an animal gives: vanilla's amount × size × sex × age, rounded at random so the average is exact.
public final class Yield {
    private Yield() {
    }

    public static double sexAge(Sex sex, boolean old, Tuning tuning) {
        double sexFactor = sex == Sex.MALE ? tuning.maleYield() : tuning.femaleYield();
        return sexFactor * (old ? tuning.oldYieldFactor() : 1.0);
    }

    // 1 at the species' base scale for the animal's sex, the line between M and L.
    public static double size(double scale, double base, double exponent) {
        if (base <= 0.0) {
            return 1.0;
        }
        return Math.pow(scale / base, exponent);
    }

    public static int round(double amount, double unitRandom) {
        if (amount <= 0.0) {
            return 0;
        }
        double whole = Math.floor(amount);
        return (int) whole + (unitRandom < amount - whole ? 1 : 0);
    }
}
