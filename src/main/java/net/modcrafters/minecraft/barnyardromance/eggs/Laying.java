package net.modcrafters.minecraft.barnyardromance.eggs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

// A hen's eggs for one overworld-clock day (GameDay): when each is due, in ticks since midnight (EggSchedule), how
// many of them are behind her, laid or skipped, and how many of those she really laid. day is -1 before her first
// day is planned. Saves from before laid was kept count every egg behind her as laid.
public record Laying(long day, List<Integer> times, int next, int laid) {
    public static final Laying NONE = new Laying(-1L, List.of(), 0, 0);
    public static final MapCodec<Laying> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.LONG.fieldOf("day").forGetter(Laying::day),
            Codec.INT.listOf().fieldOf("times").forGetter(Laying::times),
            Codec.INT.fieldOf("next").forGetter(Laying::next),
            Codec.INT.optionalFieldOf("laid", -1).forGetter(Laying::laid)
    ).apply(instance, Laying::new));
    public static final Codec<Laying> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, Laying> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            Laying::day,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()),
            Laying::times,
            ByteBufCodecs.VAR_INT,
            Laying::next,
            ByteBufCodecs.VAR_INT,
            Laying::laid,
            Laying::new
    );

    public Laying {
        times = List.copyOf(times);
        laid = laid < 0 ? next : Math.min(laid, next);
    }

    public Laying(long day, List<Integer> times, int next) {
        this(day, times, next, next);
    }

    public int planned() {
        return times.size();
    }

    // Skipped eggs (she wasn't loaded when they were due) are behind her but weren't laid.
    public Laying advance(int skipped, int laidNow) {
        int after = Math.min(times.size(), next + skipped + laidNow);
        return new Laying(day, times, after, Math.min(after, laid + laidNow));
    }

    public Laying withTimes(List<Integer> newTimes) {
        return new Laying(day, newTimes, next, laid);
    }
}
