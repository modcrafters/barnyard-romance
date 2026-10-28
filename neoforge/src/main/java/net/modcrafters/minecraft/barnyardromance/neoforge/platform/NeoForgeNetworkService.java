package net.modcrafters.minecraft.barnyardromance.neoforge.platform;

import net.modcrafters.minecraft.barnyardromance.platform.services.NetworkService;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
//? if >=1.21.6
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class NeoForgeNetworkService implements NetworkService {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        //? if >=1.21.6 {
        ClientPacketDistributor.sendToServer(payload);
        //?} else
        //PacketDistributor.sendToServer(payload);
    }
}
