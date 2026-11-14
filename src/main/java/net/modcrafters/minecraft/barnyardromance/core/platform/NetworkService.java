package net.modcrafters.minecraft.barnyardromance.core.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

// Sends the mod's own messages through the loader's networking.
public interface NetworkService {
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    // Client only.
    void sendToServer(CustomPacketPayload payload);
}
