package net.modcrafters.minecraft.barnyardromance.familiarity;

public final class Familiarity {
    public static final double GAIN = 0.2;
    public static final double THRESHOLD = 0.3;
    public static final long DAY_TICKS = 24_000L;

    private Familiarity() {
    }

    public enum Response {
        IGNORE,
        REJECT,
        ACCEPT
    }

    public enum Click {
        FEED,
        WAIT,
        SCARE
    }

    public static boolean ready(double familiarity) {
        return familiarity >= THRESHOLD;
    }

    public static double gain(double familiarity) {
        return Math.min(1.0, familiarity + GAIN);
    }

    public static Response respond(boolean food, long lastFedTick, long now) {
        if (!food) {
            return Response.IGNORE;
        }
        if (lastFedTick >= 0L && now - lastFedTick < DAY_TICKS) {
            return Response.REJECT;
        }
        return Response.ACCEPT;
    }

    public static boolean tames(boolean tamed, double familiarity) {
        return !tamed && ready(familiarity);
    }

    public static boolean allowsLove(double familiarity, boolean pregnant) {
        return ready(familiarity) && !pregnant;
    }

    public static boolean interested(boolean tracked, long lastFedTick, long now, boolean familiar) {
        if (!tracked) {
            return true;
        }
        return familiar && ticksUntilFeed(lastFedTick, now) == 0L;
    }

    public static double feedReady(long lastFedTick, long now) {
        long wait = ticksUntilFeed(lastFedTick, now);
        if (wait <= 0L) {
            return 1.0;
        }
        return 1.0 - wait / (double) DAY_TICKS;
    }

    public static long ticksUntilFeed(long lastFedTick, long now) {
        if (lastFedTick < 0L) {
            return 0L;
        }
        long elapsed = now - lastFedTick;
        if (elapsed < 0L || elapsed >= DAY_TICKS) {
            return 0L;
        }
        return DAY_TICKS - elapsed;
    }

    public static String nextFeed(long remainingTicks) {
        long totalSeconds = Math.max(0L, remainingTicks) / 20L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        String time = minutes > 0L ? minutes + "m " + seconds + "s" : seconds + "s";
        return "Can be fed again in " + time + ".";
    }

    public static Click click(boolean sneaking, boolean familiar, Response response) {
        if (!familiar && !sneaking) {
            return Click.SCARE;
        }
        return response == Response.ACCEPT ? Click.FEED : Click.WAIT;
    }

    public static double inherited(double mother) {
        return Math.clamp(mother, 0.0, 1.0) / 2.0;
    }

    public static String percent(double familiarity) {
        long points = Math.round(Math.clamp(familiarity, 0.0, 1.0) * 100.0);
        return points + "%";
    }
}
