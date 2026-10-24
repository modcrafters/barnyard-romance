package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;

public final class Familiarity {
    private Familiarity() {
    }

    public static double gain() {
        return BarnyardConfig.current().tuning().familiarityGain();
    }

    public static double threshold() {
        return BarnyardConfig.current().tuning().familiarityThreshold();
    }

    public static long dayTicks() {
        return BarnyardConfig.current().tuning().feedCooldownTicks();
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
        return familiarity >= threshold();
    }

    public static double gain(double familiarity) {
        return Math.min(1.0, familiarity + gain());
    }

    public static Response respond(boolean food, long lastFedTick, long now) {
        if (!food) {
            return Response.IGNORE;
        }
        if (lastFedTick >= 0L && now - lastFedTick < dayTicks()) {
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
        return 1.0 - wait / (double) dayTicks();
    }

    public static long ticksUntilFeed(long lastFedTick, long now) {
        if (lastFedTick < 0L) {
            return 0L;
        }
        long day = dayTicks();
        long elapsed = now - lastFedTick;
        if (elapsed < 0L || elapsed >= day) {
            return 0L;
        }
        return day - elapsed;
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
