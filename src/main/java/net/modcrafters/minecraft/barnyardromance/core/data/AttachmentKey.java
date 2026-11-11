package net.modcrafters.minecraft.barnyardromance.core.data;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

// Data kept on an animal under barnyard_romance:<id>: saved with the codec, synced to every client with the stream
// codec. Each loader registers every key in CoreAttachments.ALL and every key a feature declares. The default value
// is what NeoForge hands back when asked for data an animal doesn't have yet; Fabric attachments have none.
public record AttachmentKey<T>(String id, Codec<T> codec, StreamCodec<ByteBuf, T> streamCodec, Supplier<T> defaultValue) {
}
