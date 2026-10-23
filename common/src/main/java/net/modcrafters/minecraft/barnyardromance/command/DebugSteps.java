package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;

public final class DebugSteps {
    private DebugSteps() {
    }

    public static double familiarity(double current, int direction) {
        long points = Math.round(Math.clamp(current, 0.0, 1.0) * 100.0);
        long next = Math.clamp(points + direction * 10L, 0L, 100L);
        return next / 100.0;
    }

    public static double familiarityAt(int percent) {
        return Math.clamp(percent, 0, 100) / 100.0;
    }

    public static Pregnancy pregnancy(Pregnancy pregnancy, long now, int direction) {
        long duration = Math.max(1L, pregnancy.durationTicks());
        long step = Math.max(1L, Math.round(duration * 0.1));
        long elapsed = Math.clamp(now - pregnancy.conceivedAt(), 0L, duration);
        long nextElapsed = Math.clamp(elapsed + direction * step, 0L, duration);
        return pregnancy.withConceivedAt(now - nextElapsed);
    }

    public static Pregnancy pregnancyAt(Pregnancy pregnancy, long now, int percent) {
        long duration = Math.max(1L, pregnancy.durationTicks());
        long elapsed = Math.round(duration * (Math.clamp(percent, 0, 100) / 100.0));
        elapsed = Math.clamp(elapsed, 0L, duration);
        return pregnancy.withConceivedAt(now - elapsed);
    }
}
