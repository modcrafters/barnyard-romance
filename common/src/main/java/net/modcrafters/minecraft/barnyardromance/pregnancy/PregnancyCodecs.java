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

public final class PregnancyCodecs {
    public static final MapCodec<Pregnancy> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.LONG.fieldOf("conceivedAt").forGetter(Pregnancy::conceivedAt),
            Codec.LONG.fieldOf("durationTicks").forGetter(Pregnancy::durationTicks),
            Codec.STRING.fieldOf("fatherSpecies").forGetter(Pregnancy::fatherSpecies),
            Codec.DOUBLE.fieldOf("fatherScale").forGetter(Pregnancy::fatherScale),
            Codec.INT.fieldOf("litterSize").forGetter(Pregnancy::litterSize),
            Codec.DOUBLE.optionalFieldOf("motherFamiliarity", 0.0).forGetter(Pregnancy::motherFamiliarity),
            Codec.INT.optionalFieldOf("fatherGrowthTicks", Growth.DEFAULT).forGetter(Pregnancy::fatherGrowthTicks),
            CompoundTag.CODEC.listOf().optionalFieldOf("litter", List.of()).forGetter(Pregnancy::litter)
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
            Pregnancy::new
    );

    private PregnancyCodecs() {
    }
}
