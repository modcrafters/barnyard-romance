package net.modcrafters.minecraft.barnyardromance.vigour;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSlice;

// Vigour's field in the animal record, saved under vigour as it always has been.
public final class VigourSlices {
    private static final Codec<Vigour> VIGOUR_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("value", 1.0).forGetter(Vigour::value),
            Codec.LONG.optionalFieldOf("day", -1L).forGetter(Vigour::day),
            Codec.LONG.optionalFieldOf("rollDay", -1L).forGetter(Vigour::rollDay)
    ).apply(instance, Vigour::new));
    private static final StreamCodec<ByteBuf, Vigour> VIGOUR_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            Vigour::value,
            ByteBufCodecs.VAR_LONG,
            Vigour::day,
            ByteBufCodecs.VAR_LONG,
            Vigour::rollDay,
            Vigour::new
    );
    // A male's mating energy; see VigourRules.
    public static final AnimalSlice<Vigour> VIGOUR = new AnimalSlice<>("vigour", Vigour.NONE, VIGOUR_CODEC.optionalFieldOf("vigour", Vigour.NONE),
            VIGOUR_STREAM_CODEC);

    private VigourSlices() {
    }
}
