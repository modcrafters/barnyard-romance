package net.modcrafters.minecraft.barnyardromance.core.animal;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.StreamCodec;

import java.util.Set;
import java.util.stream.Collectors;

// A feature's own part of the animal record (see FeatureRegistrar.slice). The codec reads and writes its fields
// inline, in the same compound as the record's core fields; an animal that never set the slice reads as defaultValue.
public record AnimalSlice<T>(String name, T defaultValue, MapCodec<T> codec, StreamCodec<ByteBuf, T> streamCodec) {
    // The names the slice saves at the top of the animal's compound.
    public Set<String> fields() {
        return fieldsOf(codec);
    }

    static Set<String> fieldsOf(MapCodec<?> codec) {
        return codec.keys(NbtOps.INSTANCE)
                .map(key -> NbtOps.INSTANCE.getStringValue(key).getOrThrow())
                .collect(Collectors.toUnmodifiableSet());
    }
}
