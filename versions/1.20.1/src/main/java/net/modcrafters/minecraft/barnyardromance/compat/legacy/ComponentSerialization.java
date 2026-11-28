package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import net.minecraft.network.chat.Component;

// 1.20.5's ComponentSerialization for Minecraft 1.20.1, holding only its stream codec: chat text as FriendlyByteBuf
// has always written it.
public final class ComponentSerialization {
    public static final StreamCodec<RegistryFriendlyByteBuf, Component> STREAM_CODEC = StreamCodec.of(
            RegistryFriendlyByteBuf::writeComponent, RegistryFriendlyByteBuf::readComponent);

    private ComponentSerialization() {
    }
}
