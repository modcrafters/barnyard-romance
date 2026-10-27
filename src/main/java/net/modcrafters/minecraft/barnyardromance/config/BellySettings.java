package net.modcrafters.minecraft.barnyardromance.config;

// How much wider a pregnant female gets by the end, and how far into the pregnancy it starts to show.
public record BellySettings(double maxWiden, double start) {
    public static final BellySettings DEFAULTS = new BellySettings(0.15, 0.4);
}
