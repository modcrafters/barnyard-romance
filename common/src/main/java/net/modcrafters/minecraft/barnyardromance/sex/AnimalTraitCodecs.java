package net.modcrafters.minecraft.barnyardromance.sex;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class AnimalTraitCodecs {
    public static final Codec<Sex> SEX_CODEC = Codec.STRING.xmap(Sex::valueOf, Sex::name);
    public static final MapCodec<AnimalTraits> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SEX_CODEC.fieldOf("sex").forGetter(AnimalTraits::sex),
            Codec.DOUBLE.fieldOf("scale").forGetter(AnimalTraits::scale),
            Codec.DOUBLE.optionalFieldOf("familiarity", 0.0).forGetter(AnimalTraits::familiarity),
            Codec.LONG.optionalFieldOf("lastFedTick", -1L).forGetter(AnimalTraits::lastFedTick),
            Codec.INT.optionalFieldOf("growthTicks", Growth.DEFAULT).forGetter(AnimalTraits::growthTicks),
            Codec.LONG.optionalFieldOf("lastBirthTick", -1L).forGetter(AnimalTraits::lastBirthTick),
            Codec.STRING.optionalFieldOf("lastBirthSpecies", "").forGetter(AnimalTraits::lastBirthSpecies)
    ).apply(instance, AnimalTraits::new));
    public static final Codec<AnimalTraits> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, AnimalTraits> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(Sex::valueOf, Sex::name),
            AnimalTraits::sex,
            ByteBufCodecs.DOUBLE,
            AnimalTraits::scale,
            ByteBufCodecs.DOUBLE,
            AnimalTraits::familiarity,
            ByteBufCodecs.VAR_LONG,
            AnimalTraits::lastFedTick,
            ByteBufCodecs.VAR_INT,
            AnimalTraits::growthTicks,
            ByteBufCodecs.VAR_LONG,
            AnimalTraits::lastBirthTick,
            ByteBufCodecs.STRING_UTF8,
            AnimalTraits::lastBirthSpecies,
            AnimalTraits::new
    );

    private AnimalTraitCodecs() {
    }
}
