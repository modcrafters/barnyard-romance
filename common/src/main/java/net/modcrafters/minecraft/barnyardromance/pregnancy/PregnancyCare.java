package net.modcrafters.minecraft.barnyardromance.pregnancy;

// What food did for a pregnancy: ticks gained or lost, one speed boost, and a better-fed litter.
// The boost is read from the clock, so nothing ticks.
public record PregnancyCare(long bankedTicks, long boostFrom, long boostUntil, double boostFactor, double nutritionBonus) {
    public static final PregnancyCare NONE = new PregnancyCare(0L, 0L, 0L, 1.0, 0.0);

    // Ticks to add to the time since conception.
    public long extraTicks(long now) {
        long boosted = Math.max(0L, Math.min(now, boostUntil) - boostFrom);
        return bankedTicks + Math.round((boostFactor - 1.0) * boosted);
    }

    public boolean boosting(long now) {
        return now < boostUntil;
    }

    // Banks what the previous boost gave so far, then starts the new one.
    public PregnancyCare boost(long now, double factor, long ticks) {
        return new PregnancyCare(extraTicks(now), now, now + Math.max(0L, ticks), factor, nutritionBonus);
    }

    public PregnancyCare progress(long durationTicks, double fraction) {
        return new PregnancyCare(bankedTicks + Math.round(fraction * durationTicks), boostFrom, boostUntil, boostFactor, nutritionBonus);
    }

    public PregnancyCare improve(double amount) {
        return new PregnancyCare(bankedTicks, boostFrom, boostUntil, boostFactor, Math.clamp(nutritionBonus + amount, 0.0, 1.0));
    }

    // Keeps the better-fed litter but drops every tick gained or lost.
    public PregnancyCare withoutTicks() {
        return new PregnancyCare(0L, 0L, 0L, 1.0, nutritionBonus);
    }
}
