package net.modcrafters.minecraft.barnyardromance.core.config;

import net.modcrafters.minecraft.barnyardromance.core.compat.Jdk;
public record Range(long min, long max) {
    public static Range of(long a, long b) {
        return a <= b ? new Range(a, b) : new Range(b, a);
    }

    Range clamp(long low, long high) {
        return new Range(Jdk.clamp(min, low, high), Jdk.clamp(max, low, high));
    }
}
