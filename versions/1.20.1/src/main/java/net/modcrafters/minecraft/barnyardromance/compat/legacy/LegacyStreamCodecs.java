package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

// The stream codecs vanilla keeps on classes that 1.20.1 has without them (UUIDUtil and the id class). Stonecutter
// writes these in their place on 1.20.1.
public final class LegacyStreamCodecs {
    public static final StreamCodec<ByteBuf, UUID> UUID = StreamCodec.of(
            (buf, id) -> new FriendlyByteBuf(buf).writeUUID(id), buf -> new FriendlyByteBuf(buf).readUUID());
    public static final StreamCodec<ByteBuf, ResourceLocation> IDENTIFIER = ByteBufCodecs.STRING_UTF8.map(ResourceLocation::new, ResourceLocation::toString);

    private LegacyStreamCodecs() {
    }
}
