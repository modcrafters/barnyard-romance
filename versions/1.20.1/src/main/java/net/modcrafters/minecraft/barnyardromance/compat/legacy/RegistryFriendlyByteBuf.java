package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;

// 1.20.5's RegistryFriendlyByteBuf for Minecraft 1.20.1: a FriendlyByteBuf that knows the registries.
public class RegistryFriendlyByteBuf extends FriendlyByteBuf {
    private final RegistryAccess registryAccess;

    public RegistryFriendlyByteBuf(ByteBuf source, RegistryAccess registryAccess) {
        super(source);
        this.registryAccess = registryAccess;
    }

    public RegistryAccess registryAccess() {
        return registryAccess;
    }
}
