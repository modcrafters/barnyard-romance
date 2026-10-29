package net.modcrafters.minecraft.barnyardromance.sex;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

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
    // Saved inline as motherId and fatherId, each left out when unknown.
    public static final MapCodec<Lineage> LINEAGE_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("motherId").forGetter(lineage -> Optional.ofNullable(lineage.mother())),
            UUIDUtil.CODEC.optionalFieldOf("fatherId").forGetter(lineage -> Optional.ofNullable(lineage.father()))
    ).apply(instance, (mother, father) -> new Lineage(mother.orElse(null), father.orElse(null))));
    private static final StreamCodec<ByteBuf, Optional<UUID>> OPTIONAL_UUID_STREAM_CODEC = ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC);
    public static final StreamCodec<ByteBuf, Lineage> LINEAGE_STREAM_CODEC = StreamCodec.composite(
            OPTIONAL_UUID_STREAM_CODEC,
            lineage -> Optional.ofNullable(lineage.mother()),
            OPTIONAL_UUID_STREAM_CODEC,
            lineage -> Optional.ofNullable(lineage.father()),
            (mother, father) -> new Lineage(mother.orElse(null), father.orElse(null))
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
            LACTATION_CODEC.optionalFieldOf("lactation", Lactation.NONE).forGetter(AnimalTraits::lactation),
            LINEAGE_MAP_CODEC.forGetter(AnimalTraits::lineage)
    ).apply(instance, AnimalTraits::new));
    public static final Codec<AnimalTraits> CODEC = MAP_CODEC.codec();
    private static final StreamCodec<ByteBuf, Sex> SEX_STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Sex::valueOf, Sex::name);
    // Written field by field: StreamCodec.composite only takes six fields on 1.21.1.
    public static final StreamCodec<ByteBuf, AnimalTraits> STREAM_CODEC = StreamCodec.of(
            (buf, traits) -> {
                SEX_STREAM_CODEC.encode(buf, traits.sex());
                ByteBufCodecs.DOUBLE.encode(buf, traits.scale());
                ByteBufCodecs.DOUBLE.encode(buf, traits.familiarity());
                ByteBufCodecs.VAR_LONG.encode(buf, traits.lastFedTick());
                ByteBufCodecs.VAR_INT.encode(buf, traits.growthTicks());
                ByteBufCodecs.VAR_LONG.encode(buf, traits.lastBirthTick());
                ByteBufCodecs.STRING_UTF8.encode(buf, traits.lastBirthSpecies());
                ByteBufCodecs.VAR_LONG.encode(buf, traits.loveUntil());
                ByteBufCodecs.VAR_LONG.encode(buf, traits.cooldownUntil());
                ByteBufCodecs.DOUBLE.encode(buf, traits.weight());
                ByteBufCodecs.DOUBLE.encode(buf, traits.fertility());
                ByteBufCodecs.VAR_LONG.encode(buf, traits.fertilityTick());
                ByteBufCodecs.VAR_LONG.encode(buf, traits.bornTick());
                LACTATION_STREAM_CODEC.encode(buf, traits.lactation());
                LINEAGE_STREAM_CODEC.encode(buf, traits.lineage());
            },
            // Arguments are evaluated left to right, so this reads the fields in the order they were written.
            buf -> new AnimalTraits(
                    SEX_STREAM_CODEC.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    LACTATION_STREAM_CODEC.decode(buf),
                    LINEAGE_STREAM_CODEC.decode(buf)
            )
    );

    private AnimalTraitCodecs() {
    }
}
