package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import net.modcrafters.minecraft.barnyardromance.age.GameDay;
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

    public enum Response {
        IGNORE,
        REJECT,
        ACCEPT
    }

    public enum Click {
        FEED,
        WAIT,
        // A hurt animal fed today: it eats and heals, nothing else changes.
        HEAL_ONLY,
        SCARE
    }

    public static boolean ready(double familiarity) {
        return familiarity >= threshold();
    }

    public static double gain(double familiarity) {
        return Math.min(1.0, familiarity + gain());
    }

    public static double gain(double familiarity, double amount) {
        return Math.clamp(familiarity + Math.max(0.0, amount), 0.0, 1.0);
    }

    // Feeding times are overworld clock times (see GameDay): an animal eats once per day, and the day turns over at midnight.
    public static Response respond(boolean food, long lastFedTick, long now) {
        if (!food) {
            return Response.IGNORE;
        }
        return fedToday(lastFedTick, now) ? Response.REJECT : Response.ACCEPT;
    }

    public static boolean fedToday(long lastFedTick, long now) {
        return lastFedTick >= 0L && GameDay.sameDay(lastFedTick, now);
    }

    public static boolean tames(boolean tamed, double familiarity) {
        return !tamed && ready(familiarity);
    }

    public static boolean allowsLove(double familiarity, boolean pregnant) {
        return ready(familiarity) && !pregnant;
    }

    public static boolean interested(boolean tracked, long lastFedTick, long now, boolean familiar) {
        return interested(tracked, lastFedTick, now, familiar, false);
    }

    // A hurt animal wants food even when it was fed today.
    public static boolean interested(boolean tracked, long lastFedTick, long now, boolean familiar, boolean hurt) {
        if (!tracked) {
            return true;
        }
        return familiar && (hurt || ticksUntilFeed(lastFedTick, now) == 0L);
    }

    public static double feedReady(long lastFedTick, long now) {
        long wait = ticksUntilFeed(lastFedTick, now);
        if (wait <= 0L) {
            return 1.0;
        }
        return 1.0 - wait / (double) GameDay.TICKS;
    }

    public static long ticksUntilFeed(long lastFedTick, long now) {
        return fedToday(lastFedTick, now) ? GameDay.ticksUntilMidnight(now) : 0L;
    }

    public static Text nextFeed(long remainingTicks) {
        return Text.of("message.barnyard_romance.fed_again_in", Growth.clock(remainingTicks));
    }

    public static Click click(boolean sneaking, boolean familiar, Response response) {
        return click(sneaking, familiar, response, false);
    }

    public static Click click(boolean sneaking, boolean familiar, Response response, boolean hurt) {
        if (!familiar && !sneaking) {
            return Click.SCARE;
        }
        if (response == Response.ACCEPT) {
            return Click.FEED;
        }
        return hurt ? Click.HEAL_ONLY : Click.WAIT;
    }

    public static double inherited(double mother) {
        return Math.clamp(mother, 0.0, 1.0) / 2.0;
    }

    // A tamed animal below the threshold is grumpy (see FamiliarityCare), and says so next to its familiarity.
    public static Text text(double familiarity, boolean tamed) {
        return tamed && !ready(familiarity) ? Text.of("gui.barnyard_romance.inspector.grumpy", percent(familiarity)) : percent(familiarity);
    }

    public static Text percent(double familiarity) {
        long points = Math.round(Math.clamp(familiarity, 0.0, 1.0) * 100.0);
        return Text.of("gui.barnyard_romance.percent", points);
    }
}
