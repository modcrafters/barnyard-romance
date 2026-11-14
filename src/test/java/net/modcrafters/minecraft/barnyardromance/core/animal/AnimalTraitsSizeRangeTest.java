package net.modcrafters.minecraft.barnyardromance.core.animal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.modcrafters.minecraft.barnyardromance.BarnyardFeatures;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AnimalTraitsSizeRangeTest {
    private static final SizeSettings OLD = new SizeSettings(0.8, 1.2, 0.1);

    // The animal record's codecs take every feature's slice.
    @BeforeAll
    static void installFeatures() {
        BarnyardFeatures.ensureInstalled();
    }

    private static AnimalTraits ranged() {
        return AnimalTraits.of(Sex.MALE, 1.16).withSizeRange(OLD);
    }

    private static CompoundTag encode(AnimalTraits traits) {
        return (CompoundTag) AnimalTraitCodecs.CODEC.encodeStart(NbtOps.INSTANCE, traits).getOrThrow();
    }

    @Test
    void aRangeIsSavedAsSizeRange() {
        CompoundTag saved = encode(ranged());
        CompoundTag range = new CompoundTag();
        range.putDouble("femaleScale", 0.8);
        range.putDouble("maleScale", 1.2);
        range.putDouble("scaleVariation", 0.1);

        assertEquals(range, saved.get("sizeRange"));
        assertEquals(ranged(), AnimalTraitCodecs.CODEC.parse(NbtOps.INSTANCE, (Tag) saved).getOrThrow());
    }

    @Test
    void noRangeWritesNoField() {
        assertFalse(encode(AnimalTraits.of(Sex.MALE, 1.16)).contains("sizeRange"));
    }

    @Test
    void animalTraitsWithARangeRoundTripTheStream() {
        ByteBuf buf = Unpooled.buffer();
        AnimalTraitCodecs.STREAM_CODEC.encode(buf, ranged());

        assertEquals(ranged(), AnimalTraitCodecs.STREAM_CODEC.decode(buf));
        assertEquals(0, buf.readableBytes(), "decoding reads every byte encoding wrote");
    }

    @Test
    void theRangeTakesPartInEquality() {
        assertNotEquals(AnimalTraits.of(Sex.MALE, 1.16), ranged());
    }
}
