package net.modcrafters.minecraft.barnyardromance.sex;

public final class ScaleRoll {
    public static final double MIN_SCALE = 0.0625;
    public static final double MAX_SCALE = 16.0;

    private ScaleRoll() {
    }

    public static double roll(Sex sex, SizeSettings size, double unitRandom) {
        return fromPercent(sex, size, Math.clamp(unitRandom, 0.0, 1.0));
    }

    public static double rollWild(Sex sex, SizeSettings size, double unitRandom) {
        double percent = Math.min(Math.clamp(unitRandom, 0.0, 1.0) * 0.5, 0.5 - 1.0e-6);
        return fromPercent(sex, size, percent);
    }

    public static double percent(Sex sex, double scale, SizeSettings size) {
        double span = size.variation() * 2.0;
        if (span <= 0.0) {
            return 0.0;
        }
        double base = sex == Sex.FEMALE ? size.femaleScale() : size.maleScale();
        return Math.clamp((scale - (base - size.variation())) / span, 0.0, 1.0);
    }

    public static double fromPercent(Sex sex, SizeSettings size, double percent) {
        double base = sex == Sex.FEMALE ? size.femaleScale() : size.maleScale();
        double value = (base - size.variation()) + Math.clamp(percent, 0.0, 1.0) * size.variation() * 2.0;
        return Math.clamp(value, MIN_SCALE, MAX_SCALE);
    }
}
