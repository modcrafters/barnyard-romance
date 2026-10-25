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
}
