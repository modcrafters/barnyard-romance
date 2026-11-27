package net.modcrafters.minecraft.barnyardromance.pail;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// What a pail holds: one fluid and how many millilitres of it. It keeps no capacity and checks no tag, because the
// callers know which pail it is and which fluids it accepts. That keeps this class pure and lets a pail that holds a
// fluid nobody accepts any more still be drained. An amount of zero or less is always EMPTY, with no fluid.
public record PailContents(@Nullable Identifier fluid, int amount) {
    public static final PailContents EMPTY = new PailContents(null, 0);

    public static final Codec<PailContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("fluid").forGetter(contents -> Optional.ofNullable(contents.fluid())),
            Codec.INT.optionalFieldOf("amount", 0).forGetter(PailContents::amount)
    ).apply(instance, (fluid, amount) -> new PailContents(fluid.orElse(null), amount)));
    public static final StreamCodec<ByteBuf, PailContents> STREAM_CODEC = StreamCodec.composite(
            //? if >=1.20.5 {
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), contents -> Optional.ofNullable(contents.fluid()),
            //?} else {
            /*ByteBufCodecs.optional(net.modcrafters.minecraft.barnyardromance.compat.legacy.LegacyStreamCodecs.IDENTIFIER), contents -> Optional.ofNullable(contents.fluid()),
            *///?}
            ByteBufCodecs.VAR_INT, PailContents::amount,
            (fluid, amount) -> new PailContents(fluid.orElse(null), amount));

    // The new contents, and how much moved to get there.
    public record Moved(PailContents contents, int amount) {
    }

    public PailContents {
        if (amount <= 0 || fluid == null) {
            fluid = null;
            amount = 0;
        }
    }

    public boolean isEmpty() {
        return amount <= 0;
    }

    public boolean holds(Identifier other) {
        return fluid != null && fluid.equals(other);
    }

    // Room left in a pail of this capacity. Never negative, since a capacity can shrink under a full pail.
    public int space(int capacity) {
        return Math.max(0, capacity - amount);
    }

    // Takes in as much as fits, and nothing at all of a second fluid.
    public Moved fill(Identifier other, int requested, int capacity) {
        if (requested <= 0 || !(isEmpty() || holds(other))) {
            return new Moved(this, 0);
        }
        int moved = Math.min(requested, space(capacity));
        if (moved <= 0) {
            return new Moved(this, 0);
        }
        return new Moved(new PailContents(other, amount + moved), moved);
    }

    public Moved drain(int requested) {
        int moved = Math.min(Math.max(0, requested), amount);
        if (moved <= 0) {
            return new Moved(this, 0);
        }
        return new Moved(new PailContents(fluid, amount - moved), moved);
    }
}
