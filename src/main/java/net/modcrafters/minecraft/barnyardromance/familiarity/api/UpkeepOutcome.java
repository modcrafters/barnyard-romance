package net.modcrafters.minecraft.barnyardromance.familiarity.api;

// One day of an animal's upkeep (see DailyUpkeep): its familiarity after the day, the hunger damage the day dealt,
// whether it ran away, and whether it still counts as cared for.
public record UpkeepOutcome(double familiarity, float damage, boolean escapes, boolean cared) {
}
