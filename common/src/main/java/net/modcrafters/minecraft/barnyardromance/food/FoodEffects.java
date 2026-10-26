package net.modcrafters.minecraft.barnyardromance.food;

import java.util.Optional;

// What a food file says a food does. Empty values fall back to the defaults.
public record FoodEffects(
        Optional<Double> familiarity,
        Optional<Double> heal,
        Optional<PregnancySpeed> pregnancySpeed,
        double pregnancyProgress,
        double babyImprove
) {
    public static final FoodEffects NONE = new FoodEffects(Optional.empty(), Optional.empty(), Optional.empty(), 0.0, 0.0);

    // Pregnancy runs `factor` times as fast for `ticks` ticks.
    public record PregnancySpeed(double factor, long ticks) {
    }

    public boolean needsPregnancy() {
        return pregnancySpeed.isPresent() || pregnancyProgress != 0.0 || babyImprove > 0.0;
    }
}
