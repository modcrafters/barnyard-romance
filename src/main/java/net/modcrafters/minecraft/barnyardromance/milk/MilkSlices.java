package net.modcrafters.minecraft.barnyardromance.milk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSlice;

// Milk's field in the animal record, saved under lactation as it always has been. The codecs live here, so Lactation
// stays free of DFU classes for unit tests.
public final class MilkSlices {
    private static final Codec<Lactation> LACTATION_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("milkUntil", -1L).forGetter(Lactation::milkUntil),
            Codec.LONG.optionalFieldOf("youngUntil", -1L).forGetter(Lactation::youngUntil),
            Codec.LONG.optionalFieldOf("milkedDay", -1L).forGetter(Lactation::milkedDay),
            Codec.INT.optionalFieldOf("milkedToday", 0).forGetter(Lactation::milkedToday),
            Codec.BOOL.optionalFieldOf("graceGiven", false).forGetter(Lactation::graceGiven)
    ).apply(instance, Lactation::new));
    private static final StreamCodec<ByteBuf, Lactation> LACTATION_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            Lactation::milkUntil,
            ByteBufCodecs.VAR_LONG,
            Lactation::youngUntil,
            ByteBufCodecs.VAR_LONG,
            Lactation::milkedDay,
            ByteBufCodecs.VAR_INT,
            Lactation::milkedToday,
            ByteBufCodecs.BOOL,
            Lactation::graceGiven,
            Lactation::new
    );
    public static final AnimalSlice<Lactation> LACTATION = new AnimalSlice<>("lactation", Lactation.NONE,
            LACTATION_CODEC.optionalFieldOf("lactation", Lactation.NONE), LACTATION_STREAM_CODEC);

    private MilkSlices() {
    }
}
