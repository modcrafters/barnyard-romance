package net.modcrafters.minecraft.barnyardromance.food.api;

import java.util.Optional;

// What a food file says a food does. Empty values fall back to the defaults.
public record FoodEffects(
        Optional<Double> familiarity,
        Optional<Double> heal,
        Optional<PregnancySpeed> pregnancySpeed,
        double pregnancyProgress,
        double babyImprove,
        // Starts a dry female's milk for the keep-flowing window (Dairy Mash).
        boolean startsMilk,
        // Vigour given back to a grown male (Stud Mash); 0 for none.
        double vigour
) {
    public static final FoodEffects NONE = new FoodEffects(Optional.empty(), Optional.empty(), Optional.empty(), 0.0, 0.0, false, 0.0);

    public FoodEffects(Optional<Double> familiarity, Optional<Double> heal, Optional<PregnancySpeed> pregnancySpeed, double pregnancyProgress, double babyImprove,
                       boolean startsMilk) {
        this(familiarity, heal, pregnancySpeed, pregnancyProgress, babyImprove, startsMilk, 0.0);
    }

    public FoodEffects(Optional<Double> familiarity, Optional<Double> heal, Optional<PregnancySpeed> pregnancySpeed, double pregnancyProgress, double babyImprove) {
        this(familiarity, heal, pregnancySpeed, pregnancyProgress, babyImprove, false);
    }

    // Pregnancy runs `factor` times as fast for `ticks` ticks.
    public record PregnancySpeed(double factor, long ticks) {
    }

    public boolean needsPregnancy() {
        return pregnancySpeed.isPresent() || pregnancyProgress != 0.0 || babyImprove > 0.0;
    }
}
