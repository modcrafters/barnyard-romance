package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.sex.Growth;

public record Pregnancy(
        long conceivedAt,
        long durationTicks,
        String fatherSpecies,
        double fatherScale,
        int litterSize,
        double motherFamiliarity,
        int fatherGrowthTicks
) {
    public Pregnancy(
            long conceivedAt,
            long durationTicks,
            String fatherSpecies,
            double fatherScale,
            int litterSize,
            double motherFamiliarity
    ) {
        this(conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, Growth.DEFAULT);
    }
}
