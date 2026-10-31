package net.modcrafters.minecraft.barnyardromance.familiarity;

// The GameDay an animal last refused milking and shearing, -1 for never. A refusal lasts until midnight.
public record Refusals(long milkDay, long shearDay) {
    public static final Refusals NONE = new Refusals(-1L, -1L);

    public Refusals withMilk(long day) {
        return new Refusals(day, shearDay);
    }

    public Refusals withShear(long day) {
        return new Refusals(milkDay, day);
    }

    public boolean milkRefused(long day) {
        return milkDay >= 0L && milkDay == day;
    }

    public boolean shearRefused(long day) {
        return shearDay >= 0L && shearDay == day;
    }
}
