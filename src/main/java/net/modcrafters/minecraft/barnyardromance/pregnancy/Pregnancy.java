package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.minecraft.nbt.CompoundTag;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

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
        List<CompoundTag> litter,
        PregnancyCare care,
        // Null for pregnancies from older saves; clients never see it.
        @Nullable UUID fatherId
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
            int fatherGrowthTicks,
            double fatherWeight,
            List<CompoundTag> litter,
            PregnancyCare care
    ) {
        this(conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, fatherWeight, litter, care, null);
    }

    public Pregnancy(
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
        this(conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, fatherWeight, litter, PregnancyCare.NONE);
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

    // Debug steps set an exact elapsed time, so food's gained or lost ticks are dropped.
    public Pregnancy withConceivedAt(long tick) {
        return new Pregnancy(tick, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, fatherWeight, litter, care.withoutTicks(), fatherId);
    }

    public Pregnancy withCare(PregnancyCare next) {
        return new Pregnancy(conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, fatherWeight, litter, next, fatherId);
    }

    public Pregnancy withFatherId(@Nullable UUID id) {
        return new Pregnancy(conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, fatherWeight, litter, care, id);
    }
}
