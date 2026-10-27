package net.modcrafters.minecraft.barnyardromance.pregnancy;

public final class PregnancyClock {
    private PregnancyClock() {
    }

    public static double progress(long now, long conceivedAt, long durationTicks) {
        if (durationTicks <= 0) {
            return 1.0;
        }
        long elapsed = now - conceivedAt;
        if (elapsed <= 0) {
            return 0.0;
        }
        if (elapsed >= durationTicks) {
            return 1.0;
        }
        return elapsed / (double) durationTicks;
    }

    public static long remainingTicks(long now, long conceivedAt, long durationTicks) {
        if (durationTicks <= 0) {
            return 0;
        }
        long remaining = conceivedAt + durationTicks - now;
        if (remaining < 0) {
            return 0;
        }
        return Math.min(remaining, durationTicks);
    }

    public static boolean ready(long now, long conceivedAt, long durationTicks) {
        return durationTicks <= 0 || now - conceivedAt >= durationTicks;
    }

    // The same clock, with food's gained, lost and boosted ticks counted.
    public static double progress(long now, Pregnancy pregnancy) {
        return progress(effectiveNow(now, pregnancy), pregnancy.conceivedAt(), pregnancy.durationTicks());
    }

    public static boolean ready(long now, Pregnancy pregnancy) {
        return ready(effectiveNow(now, pregnancy), pregnancy.conceivedAt(), pregnancy.durationTicks());
    }

    // Real ticks left: a running boost covers its share faster.
    public static long remainingTicks(long now, Pregnancy pregnancy) {
        long remaining = remainingTicks(effectiveNow(now, pregnancy), pregnancy.conceivedAt(), pregnancy.durationTicks());
        PregnancyCare care = pregnancy.care();
        if (remaining <= 0L || !care.boosting(now) || care.boostFactor() <= 0.0) {
            return remaining;
        }
        long boostLeft = care.boostUntil() - now;
        double boostCovers = care.boostFactor() * boostLeft;
        if (remaining <= boostCovers) {
            return (long) Math.ceil(remaining / care.boostFactor());
        }
        return boostLeft + Math.round(remaining - boostCovers);
    }

    // Elapsed time never goes below conception, however much food slowed it.
    private static long effectiveNow(long now, Pregnancy pregnancy) {
        long elapsed = Math.max(0L, now - pregnancy.conceivedAt() + pregnancy.care().extraTicks(now));
        return pregnancy.conceivedAt() + elapsed;
    }
}
