package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.core.config.HungerSettings;
import net.modcrafters.minecraft.barnyardromance.core.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.UpkeepOutcome;

// One day of an animal's upkeep, worked out from plain numbers so the same rule serves a single day and a long catch-up.
public final class DailyUpkeep {
    // Familiarity moves in 5% steps that doubles can't hold exactly, so a hair under the threshold still counts.
    private static final double EPSILON = 1e-9;

    private DailyUpkeep() {
    }

    // pet: it never runs away (see FamiliarityCare.neverRunsAway).
    public record State(double familiarity, float health, boolean cared, boolean grown, boolean pet) {
    }

    public static UpkeepOutcome day(State state, boolean ate, double roll, HungerSettings hunger, Tuning tuning) {
        if (ate) {
            return new UpkeepOutcome(state.familiarity(), 0f, false, state.cared());
        }
        double threshold = tuning.familiarityThreshold() - EPSILON;
        double after = Math.max(0.0, state.familiarity() - tuning.familiarityHungerLoss());
        float damage = 0f;
        if (hunger.hurts() && state.familiarity() >= threshold) {
            damage = (float) Math.max(0.0, Math.min(hunger.dailyDamage(), state.health() - hunger.minHealth()));
        }
        boolean escapes = state.cared() && state.grown() && !state.pet()
                && after < tuning.escapeBelow() && roll < tuning.escapeChance();
        boolean cared = !escapes && (state.cared() || after >= threshold);
        return new UpkeepOutcome(after, damage, escapes, cared);
    }

    // The catch-up modes are for days spent away from a pen: a single day (last night) always counts.
    public static long daysToSettle(long missedDays, HungerSettings.CatchUp mode) {
        long days = Math.max(0L, missedDays);
        if (days <= 1L) {
            return days;
        }
        return switch (mode) {
            case EVERY_DAY -> days;
            case ONE_DAY -> Math.min(1L, days);
            case NONE -> 0L;
        };
    }
}
