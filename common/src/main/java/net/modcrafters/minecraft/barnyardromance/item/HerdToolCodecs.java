package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.command.HerdTool;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class HerdToolCodecs {
    public static final Codec<HerdTool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(HerdTool.Kind::valueOf, HerdTool.Kind::name).fieldOf("kind").forGetter(HerdTool::kind),
            Codec.STRING.xmap(HerdTool.Change::valueOf, HerdTool.Change::name).fieldOf("change").forGetter(HerdTool::change),
            Codec.INT.optionalFieldOf("percent", 0).forGetter(HerdTool::percent)
    ).apply(instance, HerdTool::new));

    public static final StreamCodec<ByteBuf, HerdTool> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(HerdTool.Kind::valueOf, HerdTool.Kind::name),
            HerdTool::kind,
            ByteBufCodecs.STRING_UTF8.map(HerdTool.Change::valueOf, HerdTool.Change::name),
            HerdTool::change,
            ByteBufCodecs.VAR_INT,
            HerdTool::percent,
            HerdTool::new
    );

    private HerdToolCodecs() {
    }
}
