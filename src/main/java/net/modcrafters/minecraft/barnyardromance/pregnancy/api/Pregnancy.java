package net.modcrafters.minecraft.barnyardromance.pregnancy.api;

import net.modcrafters.minecraft.barnyardromance.core.age.Growth;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
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
        @Nullable UUID fatherId,
        // Server only, like fatherId.
        SkippedTime seen,
        // The player who put the mother in love, credited with the birth's advancements. Server only, like fatherId;
        // null for no player, or a pregnancy from an older save.
        @Nullable UUID breederId,
        // The size range the father's scale was set in (see SizeRanges), or null for a pregnancy from an older save.
        @Nullable SizeSettings fatherSizeRange
) {
    public Pregnancy {
        litter = List.copyOf(litter);
    }

    // A pregnancy with only what every one has. Everything else starts as a pregnancy from an older save has it: one
    // young, no familiarity, the wild growth time, an average father, no litter, care or ids, and no stored range.
    public static Builder builder(long conceivedAt, long durationTicks, String fatherSpecies, double fatherScale) {
        return new Builder(conceivedAt, durationTicks, fatherSpecies, fatherScale);
    }

    public Builder toBuilder() {
        return new Builder(conceivedAt, durationTicks, fatherSpecies, fatherScale)
                .litterSize(litterSize)
                .motherFamiliarity(motherFamiliarity)
                .fatherGrowthTicks(fatherGrowthTicks)
                .fatherWeight(fatherWeight)
                .litter(litter)
                .care(care)
                .fatherId(fatherId)
                .seen(seen)
                .breederId(breederId)
                .fatherSizeRange(fatherSizeRange);
    }

    // Debug steps set an exact elapsed time, so food's gained or lost ticks are dropped.
    public Pregnancy withConceivedAt(long tick) {
        return toBuilder().conceivedAt(tick).care(care.withoutTicks()).build();
    }

    public Pregnancy withCare(PregnancyCare next) {
        return toBuilder().care(next).build();
    }

    public Pregnancy withFatherId(@Nullable UUID id) {
        return toBuilder().fatherId(id).build();
    }

    public Pregnancy withBreederId(@Nullable UUID id) {
        return toBuilder().breederId(id).build();
    }

    public Pregnancy withFatherSizeRange(SizeSettings range) {
        return toBuilder().fatherSizeRange(range).build();
    }

    // Moved on by ticks of skipped time: everything that was counting from a tick now counts from that much earlier.
    public Pregnancy skipped(long ticks, SkippedTime now) {
        return toBuilder().conceivedAt(conceivedAt - ticks).care(care.shifted(ticks)).seen(now).build();
    }

    public static final class Builder {
        private long conceivedAt;
        private final long durationTicks;
        private final String fatherSpecies;
        private final double fatherScale;
        private int litterSize = 1;
        private double motherFamiliarity;
        // Read from the config at build time when not set, as the wild growth time is a config number.
        private @Nullable Integer fatherGrowthTicks;
        private double fatherWeight = 0.5;
        private List<CompoundTag> litter = List.of();
        private PregnancyCare care = PregnancyCare.NONE;
        private @Nullable UUID fatherId;
        private SkippedTime seen = SkippedTime.UNSEEN;
        private @Nullable UUID breederId;
        private @Nullable SizeSettings fatherSizeRange;

        private Builder(long conceivedAt, long durationTicks, String fatherSpecies, double fatherScale) {
            this.conceivedAt = conceivedAt;
            this.durationTicks = durationTicks;
            this.fatherSpecies = fatherSpecies;
            this.fatherScale = fatherScale;
        }

        public Builder conceivedAt(long tick) {
            this.conceivedAt = tick;
            return this;
        }

        public Builder litterSize(int size) {
            this.litterSize = size;
            return this;
        }

        public Builder motherFamiliarity(double familiarity) {
            this.motherFamiliarity = familiarity;
            return this;
        }

        public Builder fatherGrowthTicks(int ticks) {
            this.fatherGrowthTicks = ticks;
            return this;
        }

        public Builder fatherWeight(double weight) {
            this.fatherWeight = weight;
            return this;
        }

        // The young, made at conception; it stays on the server.
        public Builder litter(List<CompoundTag> young) {
            this.litter = young;
            return this;
        }

        public Builder care(PregnancyCare next) {
            this.care = next;
            return this;
        }

        public Builder fatherId(@Nullable UUID id) {
            this.fatherId = id;
            return this;
        }

        public Builder seen(SkippedTime time) {
            this.seen = time;
            return this;
        }

        public Builder breederId(@Nullable UUID id) {
            this.breederId = id;
            return this;
        }

        public Builder fatherSizeRange(@Nullable SizeSettings range) {
            this.fatherSizeRange = range;
            return this;
        }

        public Pregnancy build() {
            return new Pregnancy(conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity,
                    fatherGrowthTicks != null ? fatherGrowthTicks : Growth.wild(), fatherWeight, litter, care, fatherId, seen, breederId,
                    fatherSizeRange);
        }
    }
}
