package net.modcrafters.minecraft.barnyardromance.eggs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EggDataSizeRangeTest {
    private static final SizeSettings OLD = new SizeSettings(0.8, 1.2, 0.1);
    private static final UUID MOTHER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID FATHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private static EggData egg(SizeSettings range) {
        return new EggData("minecraft:chicken", MOTHER, FATHER, 0.85, 20000, 0.5, 0.5, 1.25, 26000, 0.5, "", 24000L, 12000L, range);
    }

    private static CompoundTag encode(EggData data) {
        return (CompoundTag) EggData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
    }

    @Test
    void aRangeIsSavedAsSizeRange() {
        CompoundTag saved = encode(egg(OLD));
        CompoundTag range = new CompoundTag();
        range.putDouble("femaleScale", 0.8);
        range.putDouble("maleScale", 1.2);
        range.putDouble("scaleVariation", 0.1);

        assertEquals(range, saved.get("sizeRange"));
        assertEquals(egg(OLD), EggData.CODEC.parse(NbtOps.INSTANCE, (Tag) saved).getOrThrow());
    }

    @Test
    void noRangeWritesNoField() {
        assertFalse(encode(egg(null)).contains("sizeRange"));
    }

    @Test
    void aRangeRoundTripsTheStream() {
        ByteBuf buf = Unpooled.buffer();
        EggData.STREAM_CODEC.encode(buf, egg(OLD));

        assertEquals(egg(OLD), EggData.STREAM_CODEC.decode(buf));
        assertEquals(0, buf.readableBytes(), "decoding reads every byte encoding wrote");
    }

    @Test
    void countingDownKeepsTheRange() {
        EggData counted = egg(OLD).counted(10);

        assertEquals(OLD, counted.sizeRange());
        assertEquals(11990L, counted.remainingTicks());
    }
}
