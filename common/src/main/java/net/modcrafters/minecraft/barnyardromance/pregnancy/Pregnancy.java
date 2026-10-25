package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

// The litter is made at conception, while the father is still there to shape it.
public record Pregnancy(
        long conceivedAt,
        long durationTicks,
        String fatherSpecies,
        double fatherScale,
        int litterSize,
        double motherFamiliarity,
        int fatherGrowthTicks,
        double fatherWeight,
        List<CompoundTag> litter
) {
    public Pregnancy {
        litter = List.copyOf(litter);
    }

    public Pregnancy(
            long conceivedAt,
            long durationTicks,
            String fatherSpecies,
            double fatherScale,
            int litterSize,
            double motherFamiliarity,
            int fatherGrowthTicks
    ) {
        this(conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, 0.5);
    }

    // What clients see: the litter stays on the server.
    public Pregnancy(
            long conceivedAt,
            long durationTicks,
            String fatherSpecies,
            double fatherScale,
            int litterSize,
            double motherFamiliarity,
            int fatherGrowthTicks,
            double fatherWeight
    ) {
        this(conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, fatherWeight, List.of());
    }

    public Pregnancy(
            long conceivedAt,
            long durationTicks,
            String fatherSpecies,
            double fatherScale,
            int litterSize,
            double motherFamiliarity
    ) {
        this(conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, Growth.wild());
    }

    public Pregnancy withConceivedAt(long tick) {
        return new Pregnancy(tick, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, fatherWeight, litter);
    }
}
