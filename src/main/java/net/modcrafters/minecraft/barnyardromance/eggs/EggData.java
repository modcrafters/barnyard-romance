package net.modcrafters.minecraft.barnyardromance.eggs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

// What a fertilized egg remembers of its parents, made when it is laid, and how long it has left to hatch. Only a
// nesting box counts that down (NestingBoxBlockEntity), so an egg anywhere else is paused, however it got there.
// variant is the mother's chicken variant ("" where chickens have none).
public record EggData(
        String species,
        UUID mother,
        @Nullable UUID father,
        double motherScale,
        int motherGrowth,
        double motherWeight,
        double motherFamiliarity,
        double fatherScale,
        int fatherGrowth,
        double fatherWeight,
        String variant,
        long totalTicks,
        long remainingTicks
) {
    public static final Codec<EggData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("species").forGetter(EggData::species),
            UUIDUtil.CODEC.fieldOf("mother").forGetter(EggData::mother),
            UUIDUtil.CODEC.optionalFieldOf("father").forGetter(data -> Optional.ofNullable(data.father())),
            Codec.DOUBLE.fieldOf("motherScale").forGetter(EggData::motherScale),
            Codec.INT.fieldOf("motherGrowth").forGetter(EggData::motherGrowth),
            Codec.DOUBLE.fieldOf("motherWeight").forGetter(EggData::motherWeight),
            Codec.DOUBLE.fieldOf("motherFamiliarity").forGetter(EggData::motherFamiliarity),
            Codec.DOUBLE.fieldOf("fatherScale").forGetter(EggData::fatherScale),
            Codec.INT.fieldOf("fatherGrowth").forGetter(EggData::fatherGrowth),
            Codec.DOUBLE.fieldOf("fatherWeight").forGetter(EggData::fatherWeight),
            Codec.STRING.optionalFieldOf("variant", "").forGetter(EggData::variant),
            Codec.LONG.fieldOf("totalTicks").forGetter(EggData::totalTicks),
            Codec.LONG.fieldOf("remainingTicks").forGetter(EggData::remainingTicks)
    ).apply(instance, (species, mother, father, motherScale, motherGrowth, motherWeight, motherFamiliarity,
                       fatherScale, fatherGrowth, fatherWeight, variant, totalTicks, remainingTicks) -> new EggData(
            species, mother, father.orElse(null), motherScale, motherGrowth, motherWeight, motherFamiliarity,
            fatherScale, fatherGrowth, fatherWeight, variant, totalTicks, remainingTicks)));
    public static final StreamCodec<ByteBuf, EggData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public EggData counted(long ticks) {
        return new EggData(species, mother, father, motherScale, motherGrowth, motherWeight, motherFamiliarity,
                fatherScale, fatherGrowth, fatherWeight, variant, totalTicks, Math.max(0L, remainingTicks - ticks));
    }
}
