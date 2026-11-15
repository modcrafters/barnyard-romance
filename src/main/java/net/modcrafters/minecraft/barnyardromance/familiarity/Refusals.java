package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.familiarity.api.Refusable;

// The GameDay an animal last refused milking and shearing, -1 for never. A refusal lasts until midnight.
public record Refusals(long milkDay, long shearDay) {
    public static final Refusals NONE = new Refusals(-1L, -1L);

    public Refusals with(Refusable what, long day) {
        return switch (what) {
            case MILKING -> new Refusals(day, shearDay);
            case SHEARING -> new Refusals(milkDay, day);
        };
    }

    public boolean refused(Refusable what, long day) {
        long refusedDay = switch (what) {
            case MILKING -> milkDay;
            case SHEARING -> shearDay;
        };
        return refusedDay >= 0L && refusedDay == day;
    }
}
