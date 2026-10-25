package net.modcrafters.minecraft.barnyardromance.sex;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.Tuning;

public final class OffspringScale {
    private OffspringScale() {
    }

    public static double childScale(
            Sex babySex,
            double motherScale,
            double fatherScale,
            SizeSettings size,
            double improvementRandom,
            double nutrition
    ) {
        double average = (
                ScaleRoll.percent(Sex.FEMALE, motherScale, size) + ScaleRoll.percent(Sex.MALE, fatherScale, size)
        ) / 2.0;
        Tuning tuning = BarnyardConfig.current().tuning();
        double chance = tuning.improveBaseChance() + tuning.improveNutritionChance() * Math.clamp(nutrition, 0.0, 1.0);
        double percent = improvementRandom < chance ? average + tuning.improveStep() : average;
        return ScaleRoll.fromPercent(babySex, size, Math.clamp(percent, 0.0, 1.0));
    }
}
