package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import net.minecraft.resources.ResourceLocation;

// 1.20.5's CustomPacketPayload for Minecraft 1.20.1: a message that names its type. The Forge network service sends
// every payload over one channel and picks the codec by the type's id.
public interface CustomPacketPayload {
    Type<? extends CustomPacketPayload> type();

    record Type<T extends CustomPacketPayload>(ResourceLocation id) {
    }
}
