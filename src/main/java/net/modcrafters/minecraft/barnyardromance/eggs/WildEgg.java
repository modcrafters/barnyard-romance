package net.modcrafters.minecraft.barnyardromance.eggs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

// A fertilized egg that has been out of a nesting box: it remembers only its species and the mother's variant, so
// eggs of one kind stack. Outside a box it has no countdown (totalTicks 0); a box gives it a fresh one when it goes
// in (WildEggs.start), and taking it out again throws that away.
public record WildEgg(String species, String variant, long totalTicks, long remainingTicks) {
    public static final Codec<WildEgg> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("species").forGetter(WildEgg::species),
            Codec.STRING.optionalFieldOf("variant", "").forGetter(WildEgg::variant),
            Codec.LONG.optionalFieldOf("totalTicks", 0L).forGetter(WildEgg::totalTicks),
            Codec.LONG.optionalFieldOf("remainingTicks", 0L).forGetter(WildEgg::remainingTicks)
    ).apply(instance, WildEgg::new));
    public static final StreamCodec<ByteBuf, WildEgg> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static WildEgg of(String species, String variant) {
        return new WildEgg(species, variant, 0L, 0L);
    }

    public boolean started() {
        return totalTicks > 0L;
    }

    public WildEgg withTicks(long ticks) {
        return new WildEgg(species, variant, ticks, ticks);
    }

    public WildEgg counted(long ticks) {
        return new WildEgg(species, variant, totalTicks, Math.max(0L, remainingTicks - ticks));
    }

    public WildEgg outside() {
        return of(species, variant);
    }
}
