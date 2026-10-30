package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
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
            CARE_CODEC.optionalFieldOf("care", PregnancyCare.NONE).forGetter(Pregnancy::care),
            UUIDUtil.CODEC.optionalFieldOf("fatherId").forGetter(pregnancy -> Optional.ofNullable(pregnancy.fatherId())),
            Codec.LONG.optionalFieldOf("seenAt", Long.MIN_VALUE).forGetter(pregnancy -> pregnancy.seen().game()),
            Codec.LONG.optionalFieldOf("seenClock", Long.MIN_VALUE).forGetter(pregnancy -> pregnancy.seen().clock())
    ).apply(instance, (conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, fatherWeight, litter, care, fatherId, seenAt, seenClock) ->
            new Pregnancy(conceivedAt, durationTicks, fatherSpecies, fatherScale, litterSize, motherFamiliarity, fatherGrowthTicks, fatherWeight, litter, care, fatherId.orElse(null),
                    new SkippedTime(seenAt, seenClock))));
    public static final Codec<Pregnancy> CODEC = MAP_CODEC.codec();
    // The litter stays on the server; clients only see the fields below.
    // Written field by field: StreamCodec.composite only takes six fields on 1.21.1.
    public static final StreamCodec<ByteBuf, Pregnancy> STREAM_CODEC = StreamCodec.of(
            (buf, pregnancy) -> {
                ByteBufCodecs.VAR_LONG.encode(buf, pregnancy.conceivedAt());
                ByteBufCodecs.VAR_LONG.encode(buf, pregnancy.durationTicks());
                ByteBufCodecs.STRING_UTF8.encode(buf, pregnancy.fatherSpecies());
                ByteBufCodecs.DOUBLE.encode(buf, pregnancy.fatherScale());
                ByteBufCodecs.VAR_INT.encode(buf, pregnancy.litterSize());
                ByteBufCodecs.DOUBLE.encode(buf, pregnancy.motherFamiliarity());
                ByteBufCodecs.VAR_INT.encode(buf, pregnancy.fatherGrowthTicks());
                ByteBufCodecs.DOUBLE.encode(buf, pregnancy.fatherWeight());
                CARE_STREAM_CODEC.encode(buf, pregnancy.care());
            },
            // Arguments are evaluated left to right, so this reads the fields in the order they were written.
            buf -> new Pregnancy(
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    List.of(),
                    CARE_STREAM_CODEC.decode(buf)
            )
    );

    private PregnancyCodecs() {
    }
}
