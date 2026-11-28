package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.core.compat.Jdk;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;

public final class DebugSteps {
    private DebugSteps() {
    }

    public static double familiarity(double current, int direction) {
        long points = Math.round(Jdk.clamp(current, 0.0, 1.0) * 100.0);
        long next = Jdk.clamp(points + direction * 10L, 0L, 100L);
        return next / 100.0;
    }

    public static double familiarityAt(int percent) {
        return Jdk.clamp(percent, 0, 100) / 100.0;
    }

    public static Pregnancy pregnancy(Pregnancy pregnancy, long now, int direction) {
        long duration = Math.max(1L, pregnancy.durationTicks());
        long step = Math.max(1L, Math.round(duration * 0.1));
        // From the progress shown, which counts what food gained or lost.
        long elapsed = Jdk.clamp(now - pregnancy.conceivedAt() + pregnancy.care().extraTicks(now), 0L, duration);
        long nextElapsed = Jdk.clamp(elapsed + direction * step, 0L, duration);
        return pregnancy.withConceivedAt(now - nextElapsed);
    }

    public static Pregnancy pregnancyAt(Pregnancy pregnancy, long now, int percent) {
        long duration = Math.max(1L, pregnancy.durationTicks());
        long elapsed = Math.round(duration * (Jdk.clamp(percent, 0, 100) / 100.0));
        elapsed = Jdk.clamp(elapsed, 0L, duration);
        return pregnancy.withConceivedAt(now - elapsed);
    }
}
