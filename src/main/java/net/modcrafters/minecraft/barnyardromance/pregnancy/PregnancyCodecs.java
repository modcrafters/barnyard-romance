package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Optional;

public final class PregnancyCodecs {
    public static final Codec<PregnancyCare> CARE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("bankedTicks", 0L).forGetter(PregnancyCare::bankedTicks),
            Codec.LONG.optionalFieldOf("boostFrom", 0L).forGetter(PregnancyCare::boostFrom),
            Codec.LONG.optionalFieldOf("boostUntil", 0L).forGetter(PregnancyCare::boostUntil),
            Codec.DOUBLE.optionalFieldOf("boostFactor", 1.0).forGetter(PregnancyCare::boostFactor),
            Codec.DOUBLE.optionalFieldOf("nutritionBonus", 0.0).forGetter(PregnancyCare::nutritionBonus)
    ).apply(instance, PregnancyCare::new));
    public static final StreamCodec<ByteBuf, PregnancyCare> CARE_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            PregnancyCare::bankedTicks,
            ByteBufCodecs.VAR_LONG,
            PregnancyCare::boostFrom,
            ByteBufCodecs.VAR_LONG,
            PregnancyCare::boostUntil,
            ByteBufCodecs.DOUBLE,
            PregnancyCare::boostFactor,
            ByteBufCodecs.DOUBLE,
            PregnancyCare::nutritionBonus,
            PregnancyCare::new
    );
    public static final MapCodec<Pregnancy> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.LONG.fieldOf("conceivedAt").forGetter(Pregnancy::conceivedAt),
            Codec.LONG.fieldOf("durationTicks").forGetter(Pregnancy::durationTicks),
            Codec.STRING.fieldOf("fatherSpecies").forGetter(Pregnancy::fatherSpecies),
            Codec.DOUBLE.fieldOf("fatherScale").forGetter(Pregnancy::fatherScale),
            Codec.INT.fieldOf("litterSize").forGetter(Pregnancy::litterSize),
            Codec.DOUBLE.optionalFieldOf("motherFamiliarity", 0.0).forGetter(Pregnancy::motherFamiliarity),
            Codec.INT.optionalFieldOf("fatherGrowthTicks").xmap(ticks -> ticks.orElseGet(Growth::wild), Optional::of).forGetter(Pregnancy::fatherGrowthTicks),
            Codec.DOUBLE.optionalFieldOf("fatherWeight", 0.5).forGetter(Pregnancy::fatherWeight),
            CompoundTag.CODEC.listOf().optionalFieldOf("litter", List.of()).forGetter(Pregnancy::litter),
            CARE_CODEC.optionalFieldOf("care", PregnancyCare.NONE).forGetter(Pregnancy::care)
    ).apply(instance, Pregnancy::new));
    public static final Codec<Pregnancy> CODEC = MAP_CODEC.codec();
    // The litter stays on the server; clients only see the fields below.
    public static final StreamCodec<ByteBuf, Pregnancy> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            Pregnancy::conceivedAt,
            ByteBufCodecs.VAR_LONG,
            Pregnancy::durationTicks,
            ByteBufCodecs.STRING_UTF8,
            Pregnancy::fatherSpecies,
            ByteBufCodecs.DOUBLE,
            Pregnancy::fatherScale,
            ByteBufCodecs.VAR_INT,
            Pregnancy::litterSize,
            ByteBufCodecs.DOUBLE,
            Pregnancy::motherFamiliarity,
            ByteBufCodecs.VAR_INT,
            Pregnancy::fatherGrowthTicks,
            ByteBufCodecs.DOUBLE,
            Pregnancy::fatherWeight,
            CARE_STREAM_CODEC,
            Pregnancy::care,
            (conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, fatherWeight, care) -> new Pregnancy(
                    conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, fatherWeight, List.of(), care)
    );

    private PregnancyCodecs() {
    }
}
