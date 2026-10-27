package net.modcrafters.minecraft.barnyardromance.look;

import net.modcrafters.minecraft.barnyardromance.config.BellySettings;

public final class Belly {
    private Belly() {
    }

    // The sideways scale of a pregnant female: 1 until the start, then an S-curve up to 1 + maxWiden at the end.
    public static double widen(double progress, BellySettings belly) {
        double span = 1.0 - belly.start();
        double t = span <= 0.0 ? (progress >= 1.0 ? 1.0 : 0.0) : Math.clamp((progress - belly.start()) / span, 0.0, 1.0);
        return 1.0 + belly.maxWiden() * t * t * (3.0 - 2.0 * t);
    }
}
