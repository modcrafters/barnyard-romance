package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;

import java.util.function.BiFunction;
import java.util.function.Function;

// 1.20.5's StreamCodec for Minecraft 1.20.1: the same names, signatures and behaviour, but only what the mod calls.
// Shared code imports net.minecraft.network.codec.StreamCodec, which Stonecutter turns into this on 1.20.1.
public interface StreamCodec<B, V> extends StreamDecoder<B, V>, StreamEncoder<B, V> {
    static <B, V> StreamCodec<B, V> of(StreamEncoder<B, V> encoder, StreamDecoder<B, V> decoder) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buf) {
                return decoder.decode(buf);
            }

            @Override
            public void encode(B buf, V value) {
                encoder.encode(buf, value);
            }
        };
    }

    default <O> StreamCodec<B, O> apply(CodecOperation<B, V, O> operation) {
        return operation.apply(this);
    }

    default <O> StreamCodec<B, O> map(Function<? super V, ? extends O> to, Function<? super O, ? extends V> from) {
        StreamCodec<B, V> self = this;
        return new StreamCodec<>() {
            @Override
            public O decode(B buf) {
                return to.apply(self.decode(buf));
            }

            @Override
            public void encode(B buf, O value) {
                self.encode(buf, from.apply(value));
            }
        };
    }

    @SuppressWarnings("unchecked")
    default <S extends B> StreamCodec<S, V> cast() {
        return (StreamCodec<S, V>) this;
    }

    static <B, C, T1> StreamCodec<B, C> composite(StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
                                                  Function<T1, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buf) {
                return factory.apply(codec1.decode(buf));
            }

            @Override
            public void encode(B buf, C value) {
                codec1.encode(buf, getter1.apply(value));
            }
        };
    }

    static <B, C, T1, T2> StreamCodec<B, C> composite(StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
                                                      StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
                                                      BiFunction<T1, T2, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buf) {
                T1 v1 = codec1.decode(buf);
                T2 v2 = codec2.decode(buf);
                return factory.apply(v1, v2);
            }

            @Override
            public void encode(B buf, C value) {
                codec1.encode(buf, getter1.apply(value));
                codec2.encode(buf, getter2.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3> StreamCodec<B, C> composite(StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
                                                          StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
                                                          StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
                                                          Function3<T1, T2, T3, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buf) {
                T1 v1 = codec1.decode(buf);
                T2 v2 = codec2.decode(buf);
                T3 v3 = codec3.decode(buf);
                return factory.apply(v1, v2, v3);
            }

            @Override
            public void encode(B buf, C value) {
                codec1.encode(buf, getter1.apply(value));
                codec2.encode(buf, getter2.apply(value));
                codec3.encode(buf, getter3.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
                                                              StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
                                                              StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
                                                              StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
                                                              Function4<T1, T2, T3, T4, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buf) {
                T1 v1 = codec1.decode(buf);
                T2 v2 = codec2.decode(buf);
                T3 v3 = codec3.decode(buf);
                T4 v4 = codec4.decode(buf);
                return factory.apply(v1, v2, v3, v4);
            }

            @Override
            public void encode(B buf, C value) {
                codec1.encode(buf, getter1.apply(value));
                codec2.encode(buf, getter2.apply(value));
                codec3.encode(buf, getter3.apply(value));
                codec4.encode(buf, getter4.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
                                                                  StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
                                                                  StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
                                                                  StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
                                                                  StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
                                                                  Function5<T1, T2, T3, T4, T5, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buf) {
                T1 v1 = codec1.decode(buf);
                T2 v2 = codec2.decode(buf);
                T3 v3 = codec3.decode(buf);
                T4 v4 = codec4.decode(buf);
                T5 v5 = codec5.decode(buf);
                return factory.apply(v1, v2, v3, v4, v5);
            }

            @Override
            public void encode(B buf, C value) {
                codec1.encode(buf, getter1.apply(value));
                codec2.encode(buf, getter2.apply(value));
                codec3.encode(buf, getter3.apply(value));
                codec4.encode(buf, getter4.apply(value));
                codec5.encode(buf, getter5.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
                                                                      StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
                                                                      StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
                                                                      StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
                                                                      StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
                                                                      StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
                                                                      Function6<T1, T2, T3, T4, T5, T6, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buf) {
                T1 v1 = codec1.decode(buf);
                T2 v2 = codec2.decode(buf);
                T3 v3 = codec3.decode(buf);
                T4 v4 = codec4.decode(buf);
                T5 v5 = codec5.decode(buf);
                T6 v6 = codec6.decode(buf);
                return factory.apply(v1, v2, v3, v4, v5, v6);
            }

            @Override
            public void encode(B buf, C value) {
                codec1.encode(buf, getter1.apply(value));
                codec2.encode(buf, getter2.apply(value));
                codec3.encode(buf, getter3.apply(value));
                codec4.encode(buf, getter4.apply(value));
                codec5.encode(buf, getter5.apply(value));
                codec6.encode(buf, getter6.apply(value));
            }
        };
    }

    @FunctionalInterface
    interface CodecOperation<B, S, T> {
        StreamCodec<B, T> apply(StreamCodec<B, S> codec);
    }
}
