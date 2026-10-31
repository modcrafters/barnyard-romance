package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.config.Tuning;

// Below refusalBelow an animal sometimes won't be milked or sheared, and that try is spent until midnight.
public final class Refusal {
    // Familiarity moves in 5% steps that doubles can't hold exactly, as in Familiarity.ready.
    private static final double EPSILON = 1e-9;

    private Refusal() {
    }

    // roll is a random number from 0 to 1.
    public static boolean refuses(double familiarity, double roll, Tuning tuning) {
        return familiarity < tuning.refusalBelow() - EPSILON && roll < tuning.refusalChance();
    }
}
