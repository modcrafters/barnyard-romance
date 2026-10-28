package net.modcrafters.minecraft.barnyardromance.herd;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// One animal as the herd book shows it, worked out on the server when the book is opened.
// pregnancyPercent is -1 when not pregnant; the page's lines are already translatable components.
public record HerdEntry(
        UUID id,
        String species,
        String name,
        boolean female,
        boolean kid,
        boolean alive,
        Familiar familiar,
        boolean fedToday,
        int pregnancyPercent,
        boolean milkReady,
        Optional<UUID> mother,
        Optional<UUID> father,
        List<Component> lines
) {
    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<UUID>> OPTIONAL_UUID = ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).cast();
    private static final StreamCodec<RegistryFriendlyByteBuf, List<Component>> LINES = ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list());
    // Written field by field: StreamCodec.composite only takes six fields on 1.21.1.
    public static final StreamCodec<RegistryFriendlyByteBuf, HerdEntry> STREAM_CODEC = StreamCodec.of(
            (buf, entry) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, entry.id());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.species());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.name());
                ByteBufCodecs.BOOL.encode(buf, entry.female());
                ByteBufCodecs.BOOL.encode(buf, entry.kid());
                ByteBufCodecs.BOOL.encode(buf, entry.alive());
                ByteBufCodecs.VAR_INT.encode(buf, entry.familiar().ordinal());
                ByteBufCodecs.BOOL.encode(buf, entry.fedToday());
                ByteBufCodecs.VAR_INT.encode(buf, entry.pregnancyPercent());
                ByteBufCodecs.BOOL.encode(buf, entry.milkReady());
                OPTIONAL_UUID.encode(buf, entry.mother());
                OPTIONAL_UUID.encode(buf, entry.father());
                LINES.encode(buf, entry.lines());
            },
            // Arguments are evaluated left to right, so this reads the fields in the order they were written.
            buf -> new HerdEntry(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    Familiar.values()[Math.clamp(ByteBufCodecs.VAR_INT.decode(buf), 0, Familiar.values().length - 1)],
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    OPTIONAL_UUID.decode(buf),
                    OPTIONAL_UUID.decode(buf),
                    LINES.decode(buf)
            )
    );
}
