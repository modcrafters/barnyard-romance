package net.modcrafters.minecraft.barnyardromance.familiarity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSlice;

import java.util.Optional;

// Familiarity's fields in the animal record, saved inline under the names they have always had.
public final class FamiliaritySlices {
    private static final Codec<Hunger> HUNGER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("settledDay", -1L).forGetter(Hunger::settledDay),
            Codec.LONG.optionalFieldOf("lastAteDay", -1L).forGetter(Hunger::lastAteDay),
            Codec.LONG.optionalFieldOf("lastHungryDay", -1L).forGetter(Hunger::lastHungryDay)
    ).apply(instance, Hunger::new));
    private static final StreamCodec<ByteBuf, Hunger> HUNGER_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            Hunger::settledDay,
            ByteBufCodecs.VAR_LONG,
            Hunger::lastAteDay,
            ByteBufCodecs.VAR_LONG,
            Hunger::lastHungryDay,
            Hunger::new
    );
    // An origin saved by a later version, or mistyped, reads as unknown.
    private static final Codec<Origin> ORIGIN_CODEC = Codec.STRING.xmap(
            name -> {
                try {
                    return Origin.valueOf(name);
                } catch (IllegalArgumentException exception) {
                    return Origin.UNKNOWN;
                }
            },
            Origin::name);
    private static final StreamCodec<ByteBuf, Origin> ORIGIN_STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
            ordinal -> Origin.values()[ordinal], Origin::ordinal);
    // Saved inline as milkRefusedDay and shearRefusedDay.
    private static final MapCodec<Refusals> REFUSALS_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.LONG.optionalFieldOf("milkRefusedDay", -1L).forGetter(Refusals::milkDay),
            Codec.LONG.optionalFieldOf("shearRefusedDay", -1L).forGetter(Refusals::shearDay)
    ).apply(instance, Refusals::new));
    private static final StreamCodec<ByteBuf, Refusals> REFUSALS_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            Refusals::milkDay,
            ByteBufCodecs.VAR_LONG,
            Refusals::shearDay,
            Refusals::new
    );
    private static final Codec<Upkeep> UPKEEP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("cared", false).forGetter(Upkeep::cared),
            Codec.BOOL.optionalFieldOf("ranAway", false).forGetter(Upkeep::ranAway),
            Codec.LONG.optionalFieldOf("feederDay", -1L).forGetter(Upkeep::feederDay),
            Codec.STRING.optionalFieldOf("feeder", "").forGetter(Upkeep::feeder),
            Codec.BOOL.optionalFieldOf("inHerd", false).forGetter(Upkeep::inHerd)
    ).apply(instance, Upkeep::new));
    private static final StreamCodec<ByteBuf, Upkeep> UPKEEP_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            Upkeep::cared,
            ByteBufCodecs.BOOL,
            Upkeep::ranAway,
            ByteBufCodecs.VAR_LONG,
            Upkeep::feederDay,
            ByteBufCodecs.STRING_UTF8,
            Upkeep::feeder,
            ByteBufCodecs.BOOL,
            Upkeep::inHerd,
            Upkeep::new
    );
    private static final MapCodec<FamiliarityState> CARE_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("familiarity", 0.0).forGetter(FamiliarityState::familiarity),
            Codec.LONG.optionalFieldOf("lastFedTick", -1L).forGetter(FamiliarityState::lastFedTick),
            HUNGER_CODEC.optionalFieldOf("hunger", Hunger.NONE).forGetter(FamiliarityState::hunger),
            ORIGIN_CODEC.optionalFieldOf("origin", Origin.UNKNOWN).forGetter(FamiliarityState::origin),
            REFUSALS_MAP_CODEC.forGetter(FamiliarityState::refusals),
            // Always written, so a missing key means a save from before upkeep was kept.
            UPKEEP_CODEC.optionalFieldOf("upkeep").xmap(upkeep -> upkeep.orElse(Upkeep.UNKNOWN), Optional::of).forGetter(FamiliarityState::upkeep)
    ).apply(instance, FamiliarityState::new));
    private static final StreamCodec<ByteBuf, FamiliarityState> CARE_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            FamiliarityState::familiarity,
            ByteBufCodecs.VAR_LONG,
            FamiliarityState::lastFedTick,
            HUNGER_STREAM_CODEC,
            FamiliarityState::hunger,
            ORIGIN_STREAM_CODEC,
            FamiliarityState::origin,
            REFUSALS_STREAM_CODEC,
            FamiliarityState::refusals,
            UPKEEP_STREAM_CODEC,
            FamiliarityState::upkeep,
            FamiliarityState::new
    );
    public static final AnimalSlice<FamiliarityState> CARE = new AnimalSlice<>("care", FamiliarityState.NONE, CARE_MAP_CODEC, CARE_STREAM_CODEC);

    private FamiliaritySlices() {
    }
}
