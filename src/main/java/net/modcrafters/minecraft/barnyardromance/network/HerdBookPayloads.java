package net.modcrafters.minecraft.barnyardromance.network;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.herd.HerdEntry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// The herd book's two messages: the whole book, sent when it opens or changes; and a request to remove an animal.
public final class HerdBookPayloads {
    private HerdBookPayloads() {
    }

    // book is empty for a book that has never registered an animal.
    public record ShowBook(Optional<UUID> book, List<HerdEntry> entries) implements CustomPacketPayload {
        public static final Type<ShowBook> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "show_herd_book"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ShowBook> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).cast(),
                ShowBook::book,
                HerdEntry.STREAM_CODEC.apply(ByteBufCodecs.list()),
                ShowBook::entries,
                ShowBook::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RemoveFromBook(UUID book, UUID animal) implements CustomPacketPayload {
        public static final Type<RemoveFromBook> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "remove_from_herd_book"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RemoveFromBook> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC.cast(),
                RemoveFromBook::book,
                UUIDUtil.STREAM_CODEC.cast(),
                RemoveFromBook::animal,
                RemoveFromBook::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
