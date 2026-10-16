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
            Codec.DOUBLE.fieldOf("scale").forGetter(AnimalTraits::scale)
    ).apply(instance, AnimalTraits::new));
    public static final Codec<AnimalTraits> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, AnimalTraits> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(Sex::valueOf, Sex::name),
            AnimalTraits::sex,
            ByteBufCodecs.DOUBLE,
            AnimalTraits::scale,
            AnimalTraits::new
    );

    private AnimalTraitCodecs() {
    }
}
