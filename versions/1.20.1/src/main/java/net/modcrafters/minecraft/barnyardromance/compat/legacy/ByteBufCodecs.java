package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;

// 1.20.5's ByteBufCodecs for Minecraft 1.20.1, holding only the members the mod uses. Each reads and writes what
// FriendlyByteBuf's own methods do, which are what the vanilla codecs became.
public interface ByteBufCodecs {
    StreamCodec<ByteBuf, Boolean> BOOL = StreamCodec.of(ByteBuf::writeBoolean, ByteBuf::readBoolean);
    StreamCodec<ByteBuf, Integer> VAR_INT = StreamCodec.of((buf, value) -> friendly(buf).writeVarInt(value), buf -> friendly(buf).readVarInt());
    StreamCodec<ByteBuf, Long> VAR_LONG = StreamCodec.of((buf, value) -> friendly(buf).writeVarLong(value), buf -> friendly(buf).readVarLong());
    StreamCodec<ByteBuf, Float> FLOAT = StreamCodec.of(ByteBuf::writeFloat, ByteBuf::readFloat);
    StreamCodec<ByteBuf, Double> DOUBLE = StreamCodec.of(ByteBuf::writeDouble, ByteBuf::readDouble);
    StreamCodec<ByteBuf, String> STRING_UTF8 = StreamCodec.of((buf, value) -> friendly(buf).writeUtf(value), buf -> friendly(buf).readUtf());

    // The value saved with the codec as NBT, as vanilla's does. 1.20.1 only writes compounds on the wire, so the tag
    // goes inside one.
    static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec) {
        return StreamCodec.of(
                (buf, value) -> {
                    Tag tag = codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, error -> {
                        throw new EncoderException("Failed to encode: " + error + " " + value);
                    });
                    CompoundTag wrapper = new CompoundTag();
                    wrapper.put("value", tag);
                    friendly(buf).writeNbt(wrapper);
                },
                buf -> {
                    CompoundTag wrapper = friendly(buf).readNbt();
                    Tag tag = wrapper == null ? null : wrapper.get("value");
                    if (tag == null) {
                        throw new DecoderException("Failed to decode: no value");
                    }
                    return codec.parse(NbtOps.INSTANCE, tag).getOrThrow(false, error -> {
                        throw new DecoderException("Failed to decode: " + error + " " + tag);
                    });
                });
    }

    static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(StreamCodec<B, V> codec) {
        return StreamCodec.of(
                (buf, value) -> {
                    if (value.isPresent()) {
                        buf.writeBoolean(true);
                        codec.encode(buf, value.get());
                    } else {
                        buf.writeBoolean(false);
                    }
                },
                buf -> buf.readBoolean() ? Optional.of(codec.decode(buf)) : Optional.empty());
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> factory, StreamCodec<? super B, V> codec) {
        return StreamCodec.of(
                (buf, values) -> {
                    friendly(buf).writeVarInt(values.size());
                    for (V value : values) {
                        codec.encode(buf, value);
                    }
                },
                buf -> {
                    int count = friendly(buf).readVarInt();
                    C values = factory.apply(Math.min(count, 65536));
                    for (int i = 0; i < count; i++) {
                        values.add(codec.decode(buf));
                    }
                    return values;
                });
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec.CodecOperation<B, V, C> collection(IntFunction<C> factory) {
        return codec -> collection(factory, codec);
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
        return codec -> collection(ArrayList::new, codec);
    }

    static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(IntFunction<? extends M> factory,
                                                                                 StreamCodec<? super B, K> keyCodec,
                                                                                 StreamCodec<? super B, V> valueCodec) {
        return StreamCodec.of(
                (buf, map) -> {
                    friendly(buf).writeVarInt(map.size());
                    map.forEach((key, value) -> {
                        keyCodec.encode(buf, key);
                        valueCodec.encode(buf, value);
                    });
                },
                buf -> {
                    int count = friendly(buf).readVarInt();
                    M map = factory.apply(Math.min(count, 65536));
                    for (int i = 0; i < count; i++) {
                        K key = keyCodec.decode(buf);
                        V value = valueCodec.decode(buf);
                        map.put(key, value);
                    }
                    return map;
                });
    }

    // FriendlyByteBuf reads and writes through the buffer it wraps, so a plain ByteBuf can borrow its methods.
    private static FriendlyByteBuf friendly(ByteBuf buf) {
        return buf instanceof FriendlyByteBuf friendly ? friendly : new FriendlyByteBuf(buf);
    }
}
