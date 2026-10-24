package net.modcrafters.minecraft.barnyardromance.config;

public record Range(long min, long max) {
    public static Range of(long a, long b) {
        return a <= b ? new Range(a, b) : new Range(b, a);
    }

    Range clamp(long low, long high) {
        return new Range(Math.clamp(min, low, high), Math.clamp(max, low, high));
    }
}
