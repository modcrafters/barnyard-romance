package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// The 1.20.1 stand-ins for vanilla's stream codecs read back what they write, for every member the mod uses. Each
// round trip also checks that the codec reads exactly what it wrote, so the next field starts in the right place.
class StreamCodecShimTest {
    private record Six(boolean a, int b, long c, float d, double e, String f) {
    }

    private enum Colour {
        RED,
        GREEN
    }

    private static <T> T roundTrip(StreamCodec<? super FriendlyByteBuf, T> codec, T value) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        codec.encode(buf, value);
        buf.writeByte(42);
        T decoded = codec.decode(buf);
        assertEquals(42, buf.readByte(), "the codec read more or less than it wrote");
        assertFalse(buf.isReadable());
        return decoded;
    }

    private static <T> void assertRoundTrip(StreamCodec<? super FriendlyByteBuf, T> codec, T value) {
        assertEquals(value, roundTrip(codec, value));
    }

    @Test
    void primitivesRoundTrip() {
        assertRoundTrip(ByteBufCodecs.BOOL, true);
        assertRoundTrip(ByteBufCodecs.BOOL, false);
        assertRoundTrip(ByteBufCodecs.VAR_INT, 0);
        assertRoundTrip(ByteBufCodecs.VAR_INT, -1);
        assertRoundTrip(ByteBufCodecs.VAR_INT, Integer.MAX_VALUE);
        assertRoundTrip(ByteBufCodecs.VAR_LONG, Long.MIN_VALUE);
        assertRoundTrip(ByteBufCodecs.VAR_LONG, 123_456_789_012L);
        assertRoundTrip(ByteBufCodecs.FLOAT, 0.625F);
        assertRoundTrip(ByteBufCodecs.DOUBLE, -1.125);
        assertRoundTrip(ByteBufCodecs.STRING_UTF8, "");
        assertRoundTrip(ByteBufCodecs.STRING_UTF8, "Bessie the cow é中");
    }

    @Test
    void varIntIsWrittenAsVanillaWritesIt() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ByteBufCodecs.VAR_INT.encode(buf, 300);
        assertEquals(300, buf.readVarInt());
    }

    @Test
    void fromCodecRoundTripsThroughNbt() {
        assertRoundTrip(ByteBufCodecs.fromCodec(Codec.STRING), "minecraft:cow");
        assertRoundTrip(ByteBufCodecs.fromCodec(Codec.INT.listOf()), List.of(1, 2, 3));
    }

    @Test
    void optionalRoundTrips() {
        StreamCodec<ByteBuf, Optional<String>> codec = ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8);
        assertRoundTrip(codec, Optional.of("calf"));
        assertRoundTrip(codec, Optional.empty());
    }

    @Test
    void listAndCollectionRoundTrip() {
        assertRoundTrip(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), List.of(3, 1, 2));
        assertRoundTrip(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), List.<String>of());
        StreamCodec<ByteBuf, Set<String>> set = ByteBufCodecs.collection(LinkedHashSet::new, ByteBufCodecs.STRING_UTF8);
        assertRoundTrip(set, new LinkedHashSet<>(List.of("b", "a")));
        assertRoundTrip(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(LinkedHashSet::new)), Set.of("x"));
    }

    @Test
    void mapRoundTrips() {
        StreamCodec<ByteBuf, HashMap<String, List<String>>> codec = ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()));
        HashMap<String, List<String>> menus = new HashMap<>(Map.of("minecraft:cow", List.of("#c:crops/wheat", "minecraft:hay_block"),
                "minecraft:pig", List.of()));
        assertRoundTrip(codec, menus);
    }

    @Test
    void mapTurnsTheValue() {
        StreamCodec<ByteBuf, Colour> codec = ByteBufCodecs.STRING_UTF8.map(Colour::valueOf, Colour::name);
        assertRoundTrip(codec, Colour.GREEN);
        StreamCodec<ByteBuf, Colour> byOrdinal = ByteBufCodecs.VAR_INT.map(i -> Colour.values()[i], Colour::ordinal);
        assertRoundTrip(byOrdinal, Colour.RED);
    }

    @Test
    void compositeWithSixFieldsRoundTrips() {
        StreamCodec<ByteBuf, Six> codec = StreamCodec.composite(
                ByteBufCodecs.BOOL, Six::a,
                ByteBufCodecs.VAR_INT, Six::b,
                ByteBufCodecs.VAR_LONG, Six::c,
                ByteBufCodecs.FLOAT, Six::d,
                ByteBufCodecs.DOUBLE, Six::e,
                ByteBufCodecs.STRING_UTF8, Six::f,
                Six::new);
        assertRoundTrip(codec, new Six(true, -7, 1L << 40, 0.5F, 2.25, "heifer"));
    }

    @Test
    void ofAndCastRoundTrip() {
        StreamCodec<ByteBuf, String> codec = StreamCodec.of(
                (buf, value) -> ByteBufCodecs.STRING_UTF8.encode(buf, value),
                buf -> ByteBufCodecs.STRING_UTF8.decode(buf));
        StreamCodec<RegistryFriendlyByteBuf, String> cast = codec.cast();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), null);
        cast.encode(buf, "ram");
        assertEquals("ram", cast.decode(buf));
    }

    @Test
    void vanillaStandInsRoundTrip() {
        assertRoundTrip(LegacyStreamCodecs.UUID, UUID.fromString("0a1b2c3d-0000-4000-8000-000000000001"));
        assertRoundTrip(LegacyStreamCodecs.IDENTIFIER, new ResourceLocation("barnyard_romance", "milk"));
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), null);
        Component text = Component.literal("Due in ").append(Component.translatable("barnyard_romance.days", 3));
        ComponentSerialization.STREAM_CODEC.encode(buf, text);
        assertEquals(text, ComponentSerialization.STREAM_CODEC.decode(buf));
    }
}
