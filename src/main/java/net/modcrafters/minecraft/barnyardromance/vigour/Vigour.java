package net.modcrafters.minecraft.barnyardromance.vigour;

// A male's mating energy, 0 to 1. It is worked out lazily, like hunger: value is what he had on day (a GameDay, the
// last one it was settled on). rollDay is the last day he rolled for a fight without a female. -1 means never or
// unknown. A male nobody has settled yet starts full.
public record Vigour(double value, long day, long rollDay) {
    public static final Vigour NONE = new Vigour(1.0, -1L, -1L);

    public Vigour withValue(double value) {
        return new Vigour(value, day, rollDay);
    }

    public Vigour withRollDay(long rollDay) {
        return new Vigour(value, day, rollDay);
    }
}
