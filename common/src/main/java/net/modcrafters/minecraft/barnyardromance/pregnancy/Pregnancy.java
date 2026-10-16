package net.modcrafters.minecraft.barnyardromance.pregnancy;

public record Pregnancy(
        long conceivedAt,
        long durationTicks,
        String fatherSpecies,
        double fatherScale,
        int litterSize
) {
}
