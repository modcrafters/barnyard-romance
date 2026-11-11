package net.modcrafters.minecraft.barnyardromance.core.animal;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.modcrafters.minecraft.barnyardromance.core.age.Growth;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

// The animal record is saved as one compound: the core fields, then each feature's slice inline beside them, under the
// names they have always had. It is synced as the core fields, then each slice in registration order.
public final class AnimalTraitCodecs {
    public static final Codec<Sex> SEX_CODEC = Codec.STRING.xmap(Sex::valueOf, Sex::name);
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
    private static final MapCodec<AnimalTraits> CORE_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SEX_CODEC.fieldOf("sex").forGetter(AnimalTraits::sex),
            Codec.DOUBLE.fieldOf("scale").forGetter(AnimalTraits::scale),
            // Read when decoding, not when the codec is built: the config isn't loaded yet then.
            Codec.INT.optionalFieldOf("growthTicks").xmap(ticks -> ticks.orElseGet(Growth::wild), Optional::of).forGetter(AnimalTraits::growthTicks),
            Codec.DOUBLE.optionalFieldOf("weight", 0.5).forGetter(AnimalTraits::weight),
            Codec.LONG.optionalFieldOf("bornTick", -1L).forGetter(AnimalTraits::bornTick),
            LINEAGE_MAP_CODEC.forGetter(AnimalTraits::lineage)
    ).apply(instance, (sex, scale, growthTicks, weight, bornTick, lineage) ->
            new AnimalTraits(sex, scale, growthTicks, weight, bornTick, lineage, Map.of())));
    private static final StreamCodec<ByteBuf, Sex> SEX_STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Sex::valueOf, Sex::name);
    private static final StreamCodec<ByteBuf, AnimalTraits> CORE_STREAM_CODEC = StreamCodec.composite(
            SEX_STREAM_CODEC,
            AnimalTraits::sex,
            ByteBufCodecs.DOUBLE,
            AnimalTraits::scale,
            ByteBufCodecs.VAR_INT,
            AnimalTraits::growthTicks,
            ByteBufCodecs.DOUBLE,
            AnimalTraits::weight,
            ByteBufCodecs.VAR_LONG,
            AnimalTraits::bornTick,
            LINEAGE_STREAM_CODEC,
            AnimalTraits::lineage,
            (sex, scale, growthTicks, weight, bornTick, lineage) -> new AnimalTraits(sex, scale, growthTicks, weight, bornTick, lineage, Map.of())
    );
    // Built once, on first use: every feature must have registered its slice by then.
    private static final Supplier<MapCodec<AnimalTraits>> BUILT_MAP_CODEC = Suppliers.memoize(
            () -> mapCodecOf(FeaturesRepository.features().slices()));
    private static final Supplier<StreamCodec<ByteBuf, AnimalTraits>> BUILT_STREAM_CODEC = Suppliers.memoize(
            () -> streamCodecOf(FeaturesRepository.features().slices()));
    // For holders made before the features are installed, such as the attachment key: each use goes to mapCodec().
    public static final Codec<AnimalTraits> CODEC = new MapCodec<AnimalTraits>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return mapCodec().keys(ops);
        }

        @Override
        public <T> DataResult<AnimalTraits> decode(DynamicOps<T> ops, MapLike<T> input) {
            return mapCodec().decode(ops, input);
        }

        @Override
        public <T> RecordBuilder<T> encode(AnimalTraits input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return mapCodec().encode(input, ops, prefix);
        }

        @Override
        public String toString() {
            return "AnimalTraits";
        }
    }.codec();
    // As CODEC, for syncing: each use goes to streamCodec().
    public static final StreamCodec<ByteBuf, AnimalTraits> STREAM_CODEC = StreamCodec.of(
            (buf, traits) -> streamCodec().encode(buf, traits),
            buf -> streamCodec().decode(buf));

    private AnimalTraitCodecs() {
    }

    public static MapCodec<AnimalTraits> mapCodec() {
        return BUILT_MAP_CODEC.get();
    }

    public static StreamCodec<ByteBuf, AnimalTraits> streamCodec() {
        return BUILT_STREAM_CODEC.get();
    }

    // The names the core fields are saved under; no slice may use them.
    public static Set<String> coreFields() {
        return AnimalSlice.fieldsOf(CORE_MAP_CODEC);
    }

    static MapCodec<AnimalTraits> mapCodecOf(List<AnimalSlice<?>> slices) {
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.concat(CORE_MAP_CODEC.keys(ops), slices.stream().flatMap(slice -> slice.codec().keys(ops)));
            }

            @Override
            public <T> DataResult<AnimalTraits> decode(DynamicOps<T> ops, MapLike<T> input) {
                DataResult<AnimalTraits> traits = CORE_MAP_CODEC.decode(ops, input);
                for (AnimalSlice<?> slice : slices) {
                    traits = decodeSlice(traits, slice, ops, input);
                }
                return traits;
            }

            @Override
            public <T> RecordBuilder<T> encode(AnimalTraits input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                RecordBuilder<T> builder = CORE_MAP_CODEC.encode(input, ops, prefix);
                for (AnimalSlice<?> slice : slices) {
                    builder = encodeSlice(slice, input, ops, builder);
                }
                return builder;
            }

            @Override
            public String toString() {
                return "AnimalTraits";
            }
        };
    }

    static StreamCodec<ByteBuf, AnimalTraits> streamCodecOf(List<AnimalSlice<?>> slices) {
        return StreamCodec.of(
                (buf, traits) -> {
                    CORE_STREAM_CODEC.encode(buf, traits);
                    for (AnimalSlice<?> slice : slices) {
                        writeSlice(buf, slice, traits);
                    }
                },
                buf -> {
                    AnimalTraits traits = CORE_STREAM_CODEC.decode(buf);
                    for (AnimalSlice<?> slice : slices) {
                        traits = readSlice(buf, slice, traits);
                    }
                    return traits;
                });
    }

    private static <T, V> DataResult<AnimalTraits> decodeSlice(DataResult<AnimalTraits> traits, AnimalSlice<V> slice, DynamicOps<T> ops, MapLike<T> input) {
        return traits.apply2((decoded, value) -> decoded.with(slice, value), slice.codec().decode(ops, input));
    }

    private static <T, V> RecordBuilder<T> encodeSlice(AnimalSlice<V> slice, AnimalTraits traits, DynamicOps<T> ops, RecordBuilder<T> builder) {
        return slice.codec().encode(traits.get(slice), ops, builder);
    }

    private static <V> void writeSlice(ByteBuf buf, AnimalSlice<V> slice, AnimalTraits traits) {
        slice.streamCodec().encode(buf, traits.get(slice));
    }

    private static <V> AnimalTraits readSlice(ByteBuf buf, AnimalSlice<V> slice, AnimalTraits traits) {
        return traits.with(slice, slice.streamCodec().decode(buf));
    }
}
