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
        this(conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, List.of());
    }

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

    public Pregnancy withConceivedAt(long tick) {
        return new Pregnancy(tick, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, litter);
    }
}
