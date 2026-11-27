package net.modcrafters.minecraft.barnyardromance.forge.gametest;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.modcrafters.minecraft.barnyardromance.compat.legacy.CustomPacketPayload;
import net.modcrafters.minecraft.barnyardromance.forge.platform.ForgeNetworkService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// The packets the server sends a mock player, while a test listens (see mixin/SentPacketsMixin). A mock player's
// connection goes nowhere, so this is the only place to see what a client would get. It listens on the connection,
// which Forge's own sends (PacketDistributor.PLAYER) use directly.
public final class SentPackets {
    private static final Map<Connection, List<Packet<?>>> LISTENING = new ConcurrentHashMap<>();

    private SentPackets() {
    }

    // From now until stop, every packet sent to the player, in order.
    public static List<Packet<?>> capture(ServerPlayer player) {
        List<Packet<?>> sent = Collections.synchronizedList(new ArrayList<>());
        LISTENING.put(player.connection.connection, sent);
        return sent;
    }

    public static void stop(ServerPlayer player) {
        LISTENING.remove(player.connection.connection);
    }

    public static void sent(Connection connection, Packet<?> packet) {
        List<Packet<?>> sent = LISTENING.get(connection);
        if (sent != null) {
            sent.add(packet);
        }
    }

    // The mod's own payloads among the packets, bundles opened.
    public static List<CustomPacketPayload> payloads(List<Packet<?>> packets) {
        List<CustomPacketPayload> payloads = new ArrayList<>();
        synchronized (packets) {
            for (Packet<?> packet : packets) {
                addPayloads(packet, payloads);
            }
        }
        return payloads;
    }

    private static void addPayloads(Packet<?> packet, List<CustomPacketPayload> payloads) {
        if (packet instanceof BundlePacket<?> bundle) {
            for (Packet<?> inner : bundle.subPackets()) {
                addPayloads(inner, payloads);
            }
            return;
        }
        ForgeNetworkService.read(packet).ifPresent(payloads::add);
    }
}
