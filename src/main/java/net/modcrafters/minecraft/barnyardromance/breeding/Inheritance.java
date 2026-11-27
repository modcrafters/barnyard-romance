package net.modcrafters.minecraft.barnyardromance.breeding;

import net.modcrafters.minecraft.barnyardromance.core.compat.Jdk;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.config.Tuning;

// Every inherited trait, as a 0-1 percentage: the parents' average, a chance to improve, and a small spread.
public final class Inheritance {
    private Inheritance() {
    }

    // A spreadRandom of 0.5 adds no spread.
    public static double child(double mother, double father, double improveRandom, double spreadRandom, double nutrition) {
        Tuning tuning = BarnyardConfig.current().tuning();
        double average = (Jdk.clamp(mother, 0.0, 1.0) + Jdk.clamp(father, 0.0, 1.0)) / 2.0;
        double chance = tuning.improveBaseChance() + tuning.improveNutritionChance() * Jdk.clamp(nutrition, 0.0, 1.0);
        double next = improveRandom < chance ? average + tuning.improveStep() : average;
        next += (Jdk.clamp(spreadRandom, 0.0, 1.0) * 2.0 - 1.0) * tuning.inheritSpread();
        return Jdk.clamp(next, 0.0, 1.0);
    }
}
