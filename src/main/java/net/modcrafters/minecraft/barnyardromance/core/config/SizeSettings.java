package net.modcrafters.minecraft.barnyardromance.core.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SizeSettings(double femaleScale, double maleScale, double variation) {
    // Saved under the names the config file uses.
    public static final Codec<SizeSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("femaleScale").forGetter(SizeSettings::femaleScale),
            Codec.DOUBLE.fieldOf("maleScale").forGetter(SizeSettings::maleScale),
            Codec.DOUBLE.fieldOf("scaleVariation").forGetter(SizeSettings::variation)
    ).apply(instance, SizeSettings::new));
    public static final StreamCodec<ByteBuf, SizeSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            SizeSettings::femaleScale,
            ByteBufCodecs.DOUBLE,
            SizeSettings::maleScale,
            ByteBufCodecs.DOUBLE,
            SizeSettings::variation,
            SizeSettings::new
    );

    public double smallest() {
        return femaleScale - variation;
    }

    public double largest() {
        return maleScale + variation;
    }
}
