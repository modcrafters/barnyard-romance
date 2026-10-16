package net.modcrafters.minecraft.barnyardromance.sex;

public final class OffspringScale {
    public static final double BASE_IMPROVEMENT_CHANCE = 0.35;
    public static final double NUTRITION_CHANCE = 0.45;
    public static final double IMPROVEMENT = 0.25;

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
        double chance = BASE_IMPROVEMENT_CHANCE + NUTRITION_CHANCE * Math.clamp(nutrition, 0.0, 1.0);
        double percent = improvementRandom < chance ? average + IMPROVEMENT : average;
        return ScaleRoll.fromPercent(babySex, size, Math.clamp(percent, 0.0, 1.0));
    }
}
