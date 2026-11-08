package net.modcrafters.minecraft.barnyardromance.age;

// Days on the overworld clock, which wakes sleepers at 0 and reaches midnight at 18,000.
// A day turns over at midnight, so once-a-day actions reset then instead of drifting with a timer.
public final class GameDay {
    public static final long TICKS = 24_000L;
    private static final long MIDNIGHT = 18_000L;

    private GameDay() {
    }

    public static long of(long clock) {
        return Math.floorDiv(clock + (TICKS - MIDNIGHT), TICKS);
    }

    public static long ticksUntilMidnight(long clock) {
        return (of(clock) + 1L) * TICKS - (TICKS - MIDNIGHT) - clock;
    }

    // Sunrise is clock 0 of each 24,000, 6,000 ticks into the day; the day runs on from there to midnight.
    public static boolean afterSunrise(long clock) {
        return Math.floorMod(clock, TICKS) < MIDNIGHT;
    }

    public static boolean sameDay(long clockA, long clockB) {
        return of(clockA) == of(clockB);
    }
}
