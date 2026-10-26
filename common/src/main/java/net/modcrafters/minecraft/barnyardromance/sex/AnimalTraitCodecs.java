package net.modcrafters.minecraft.barnyardromance.sex;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public final class AnimalTraitCodecs {
    public static final Codec<Sex> SEX_CODEC = Codec.STRING.xmap(Sex::valueOf, Sex::name);
    // Lactation's codecs live here, so the record stays free of DFU classes for unit tests.
    public static final Codec<Lactation> LACTATION_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("milkUntil", -1L).forGetter(Lactation::milkUntil),
            Codec.LONG.optionalFieldOf("youngUntil", -1L).forGetter(Lactation::youngUntil),
            Codec.LONG.optionalFieldOf("milkedDay", -1L).forGetter(Lactation::milkedDay),
            Codec.INT.optionalFieldOf("milkedToday", 0).forGetter(Lactation::milkedToday),
            Codec.BOOL.optionalFieldOf("graceGiven", false).forGetter(Lactation::graceGiven)
    ).apply(instance, Lactation::new));
    public static final StreamCodec<ByteBuf, Lactation> LACTATION_STREAM_CODEC = StreamCodec.composite(
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
    public static final MapCodec<AnimalTraits> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SEX_CODEC.fieldOf("sex").forGetter(AnimalTraits::sex),
            Codec.DOUBLE.fieldOf("scale").forGetter(AnimalTraits::scale),
            Codec.DOUBLE.optionalFieldOf("familiarity", 0.0).forGetter(AnimalTraits::familiarity),
            Codec.LONG.optionalFieldOf("lastFedTick", -1L).forGetter(AnimalTraits::lastFedTick),
            Codec.INT.optionalFieldOf("growthTicks").xmap(ticks -> ticks.orElseGet(Growth::wild), Optional::of).forGetter(AnimalTraits::growthTicks),
            Codec.LONG.optionalFieldOf("lastBirthTick", -1L).forGetter(AnimalTraits::lastBirthTick),
            Codec.STRING.optionalFieldOf("lastBirthSpecies", "").forGetter(AnimalTraits::lastBirthSpecies),
            Codec.LONG.optionalFieldOf("loveUntil", -1L).forGetter(AnimalTraits::loveUntil),
            Codec.LONG.optionalFieldOf("cooldownUntil", -1L).forGetter(AnimalTraits::cooldownUntil),
            Codec.DOUBLE.optionalFieldOf("weight", 0.5).forGetter(AnimalTraits::weight),
            Codec.DOUBLE.optionalFieldOf("fertility", 1.0).forGetter(AnimalTraits::fertility),
            Codec.LONG.optionalFieldOf("fertilityTick", -1L).forGetter(AnimalTraits::fertilityTick),
            Codec.LONG.optionalFieldOf("bornTick", -1L).forGetter(AnimalTraits::bornTick),
            LACTATION_CODEC.optionalFieldOf("lactation", Lactation.NONE).forGetter(AnimalTraits::lactation)
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
            ByteBufCodecs.VAR_LONG,
            AnimalTraits::loveUntil,
            ByteBufCodecs.VAR_LONG,
            AnimalTraits::cooldownUntil,
            ByteBufCodecs.DOUBLE,
            AnimalTraits::weight,
            ByteBufCodecs.DOUBLE,
            AnimalTraits::fertility,
            ByteBufCodecs.VAR_LONG,
            AnimalTraits::fertilityTick,
            ByteBufCodecs.VAR_LONG,
            AnimalTraits::bornTick,
            LACTATION_STREAM_CODEC,
            AnimalTraits::lactation,
            AnimalTraits::new
    );

    private AnimalTraitCodecs() {
    }
}
