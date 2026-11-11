package net.modcrafters.minecraft.barnyardromance.pregnancy;

// Breeding's slice of the animal record (see PregnancySlices.BREEDING). Ticks are game ticks, -1 for never or none:
// the last birth and its species, the tick love mode ends, the tick the breeding rest ends, and fertility as of
// fertilityTick, from which it recovers with the clock (see Fertility).
public record BreedingState(long lastBirthTick, String lastBirthSpecies, long loveUntil, long cooldownUntil, double fertility, long fertilityTick) {
    public static final BreedingState NONE = new BreedingState(-1L, "", -1L, -1L, 1.0, -1L);

    public BreedingState gaveBirth(long tick, String species) {
        return new BreedingState(tick, species, loveUntil, cooldownUntil, fertility, fertilityTick);
    }

    public BreedingState withLove(long until) {
        return new BreedingState(lastBirthTick, lastBirthSpecies, until, cooldownUntil, fertility, fertilityTick);
    }

    public BreedingState withCooldown(long until) {
        return new BreedingState(lastBirthTick, lastBirthSpecies, loveUntil, until, fertility, fertilityTick);
    }

    public BreedingState withFertility(double fertility, long tick) {
        return new BreedingState(lastBirthTick, lastBirthSpecies, loveUntil, cooldownUntil, fertility, tick);
    }
}
