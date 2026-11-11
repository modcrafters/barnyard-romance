package net.modcrafters.minecraft.barnyardromance.pregnancy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSlice;

// Breeding's fields in the animal record, saved inline under the names they have always had.
public final class PregnancySlices {
    private static final MapCodec<BreedingState> BREEDING_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.LONG.optionalFieldOf("lastBirthTick", -1L).forGetter(BreedingState::lastBirthTick),
            Codec.STRING.optionalFieldOf("lastBirthSpecies", "").forGetter(BreedingState::lastBirthSpecies),
            Codec.LONG.optionalFieldOf("loveUntil", -1L).forGetter(BreedingState::loveUntil),
            Codec.LONG.optionalFieldOf("cooldownUntil", -1L).forGetter(BreedingState::cooldownUntil),
            Codec.DOUBLE.optionalFieldOf("fertility", 1.0).forGetter(BreedingState::fertility),
            Codec.LONG.optionalFieldOf("fertilityTick", -1L).forGetter(BreedingState::fertilityTick)
    ).apply(instance, BreedingState::new));
    private static final StreamCodec<ByteBuf, BreedingState> BREEDING_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            BreedingState::lastBirthTick,
            ByteBufCodecs.STRING_UTF8,
            BreedingState::lastBirthSpecies,
            ByteBufCodecs.VAR_LONG,
            BreedingState::loveUntil,
            ByteBufCodecs.VAR_LONG,
            BreedingState::cooldownUntil,
            ByteBufCodecs.DOUBLE,
            BreedingState::fertility,
            ByteBufCodecs.VAR_LONG,
            BreedingState::fertilityTick,
            BreedingState::new
    );
    public static final AnimalSlice<BreedingState> BREEDING = new AnimalSlice<>("breeding", BreedingState.NONE, BREEDING_MAP_CODEC,
            BREEDING_STREAM_CODEC);

    private PregnancySlices() {
    }
}
