package net.modcrafters.minecraft.barnyardromance.feed;

// One item is one animal's daily meal, as in the feeder window. A nonempty store never looks empty,
// and the full stage requires the configured number of herd-days to actually be stocked.
public final class FeederFill {
    public static final int LEVELS = 4;

    private FeederFill() {
    }

    public static int level(int food, int animals, int fullDays) {
        if (food <= 0) {
            return 0;
        }
        long target = (long) Math.max(1, animals) * Math.max(1, fullDays);
        return (int) Math.clamp((long) food * LEVELS / target, 1L, LEVELS);
    }
}
