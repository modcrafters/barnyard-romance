package net.modcrafters.minecraft.barnyardromance.pregnancy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyCare;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyCodecs;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.SkippedTime;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class PregnancySizeRangeTest {
    private static final SizeSettings OLD = new SizeSettings(0.8, 1.2, 0.1);
    private static final UUID FATHER = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID BREEDER = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private static List<CompoundTag> litter() {
        CompoundTag calf = new CompoundTag();
        calf.putString("id", "minecraft:cow");
        return List.of(calf);
    }

    private static Pregnancy ranged() {
        return Pregnancy.builder(1_000L, 48_000L, "minecraft:cow", 1.25).motherFamiliarity(0.5).fatherGrowthTicks(30_000).fatherWeight(0.75).litter(litter())
                .fatherId(FATHER)
                .breederId(BREEDER)
                .fatherSizeRange(OLD)
                .build();
    }

    private static CompoundTag encode(Pregnancy pregnancy) {
        return (CompoundTag) PregnancyCodecs.CODEC.encodeStart(NbtOps.INSTANCE, pregnancy).getOrThrow();
    }

    @Test
    void aFatherRangeIsSavedAsFatherSizeRange() {
        CompoundTag saved = encode(ranged());
        CompoundTag range = new CompoundTag();
        range.putDouble("femaleScale", 0.8);
        range.putDouble("maleScale", 1.2);
        range.putDouble("scaleVariation", 0.1);

        assertEquals(range, saved.get("fatherSizeRange"));
        assertEquals(ranged(), PregnancyCodecs.CODEC.parse(NbtOps.INSTANCE, (Tag) saved).getOrThrow());
    }

    @Test
    void noFatherRangeWritesNoField() {
        assertFalse(encode(Pregnancy.builder(0L, 1L, "minecraft:cow", 1.0).build()).contains("fatherSizeRange"));
    }

    @Test
    void aFatherRangeRoundTripsTheStream() {
        ByteBuf buf = Unpooled.buffer();
        PregnancyCodecs.STREAM_CODEC.encode(buf, ranged());

        // Clients see neither the litter nor the server-only ids, as before; they do see the father's range.
        Pregnancy expected = Pregnancy.builder(1_000L, 48_000L, "minecraft:cow", 1.25).motherFamiliarity(0.5).fatherGrowthTicks(30_000).fatherWeight(0.75)
                .fatherSizeRange(OLD)
                .build();
        assertEquals(expected, PregnancyCodecs.STREAM_CODEC.decode(buf));
        assertEquals(0, buf.readableBytes(), "decoding reads every byte encoding wrote");
    }

    @Test
    void noFatherRangeRoundTripsTheStream() {
        ByteBuf buf = Unpooled.buffer();
        PregnancyCodecs.STREAM_CODEC.encode(buf, Pregnancy.builder(0L, 1L, "minecraft:cow", 1.0).build());

        assertNull(PregnancyCodecs.STREAM_CODEC.decode(buf).fatherSizeRange());
        assertEquals(0, buf.readableBytes(), "decoding reads every byte encoding wrote");
    }

    @Test
    void everyCopyKeepsTheFatherRange() {
        Pregnancy pregnancy = ranged();

        assertEquals(OLD, pregnancy.withConceivedAt(5L).fatherSizeRange());
        assertEquals(OLD, pregnancy.withFatherId(null).fatherSizeRange());
        assertEquals(OLD, pregnancy.withBreederId(null).fatherSizeRange());
        assertEquals(OLD, pregnancy.withCare(PregnancyCare.NONE.improve(0.3)).fatherSizeRange());
        assertEquals(OLD, pregnancy.skipped(100L, new SkippedTime(2_000L, 3_000L)).fatherSizeRange());
    }
}
