package net.modcrafters.minecraft.barnyardromance.forge.platform;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.modcrafters.minecraft.barnyardromance.compat.legacy.CustomPacketPayload;
import net.modcrafters.minecraft.barnyardromance.compat.legacy.RegistryFriendlyByteBuf;
import net.modcrafters.minecraft.barnyardromance.compat.legacy.StreamCodec;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.platform.NetworkService;
import net.modcrafters.minecraft.barnyardromance.forge.client.BarnyardRomanceForgeClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// Every payload goes over one channel, barnyard_romance:main. Each payload type gets a discriminator in the order it's
// registered, which both sides do the same way, and is written with its own stream codec (the 1.20.1 stand-ins).
public class ForgeNetworkService implements NetworkService {
    public static final ResourceLocation CHANNEL_ID = new ResourceLocation(Constants.MOD_ID, "main");
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(CHANNEL_ID, () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    // Each registered payload's codec, by its discriminator.
    private static final List<StreamCodec<? super RegistryFriendlyByteBuf, ? extends CustomPacketPayload>> CODECS = new ArrayList<>();
    // What the client does with each payload the server sends, which the client sets up (see BarnyardRomanceForgeClient).
    private static final Map<CustomPacketPayload.Type<?>, Consumer<CustomPacketPayload>> CLIENT_HANDLERS = new ConcurrentHashMap<>();

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        CHANNEL.sendToServer(payload);
    }

    // To every player tracking the entity, and the entity itself if it's a player.
    public static void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), payload);
    }

    // A payload the server sends; the client handles it on its main thread with what it gave onClient.
    public static <T extends CustomPacketPayload> void toClient(CustomPacketPayload.Type<T> type, Class<T> payload,
                                                                StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        register(payload, codec, NetworkDirection.PLAY_TO_CLIENT, (message, context) -> {
            Consumer<CustomPacketPayload> handler = CLIENT_HANDLERS.get(type);
            if (handler != null) {
                handler.accept(message);
            }
        });
    }

    // A payload the client sends; the server handles it on its main thread.
    public static <T extends CustomPacketPayload> void toServer(Class<T> payload, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                                BiConsumer<T, ServerPlayer> handler) {
        register(payload, codec, NetworkDirection.PLAY_TO_SERVER, (message, context) -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                handler.accept(message, sender);
            }
        });
    }

    // Client only.
    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> void onClient(CustomPacketPayload.Type<T> type, Consumer<T> handler) {
        CLIENT_HANDLERS.put(type, payload -> handler.accept((T) payload));
    }

    // The mod's payload in a packet on its way to a client, if it is one. For game tests, which see packets.
    public static Optional<CustomPacketPayload> read(Packet<?> packet) {
        if (!(packet instanceof ClientboundCustomPayloadPacket custom) || !custom.getName().equals(CHANNEL_ID)) {
            return Optional.empty();
        }
        FriendlyByteBuf data = custom.getData();
        try {
            int index = data.readUnsignedByte();
            return Optional.of(CODECS.get(index).decode(new RegistryFriendlyByteBuf(data, registries())));
        } finally {
            data.release();
        }
    }

    private static <T extends CustomPacketPayload> void register(Class<T> payload, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                                 NetworkDirection direction,
                                                                 BiConsumer<T, net.minecraftforge.network.NetworkEvent.Context> handler) {
        int index = CODECS.size();
        CODECS.add(codec);
        CHANNEL.messageBuilder(payload, index, direction)
                .encoder((message, buf) -> codec.encode(new RegistryFriendlyByteBuf(buf, registries()), message))
                .decoder(buf -> codec.decode(new RegistryFriendlyByteBuf(buf, registries())))
                .consumerMainThread((message, context) -> handler.accept(message, context.get()))
                .add();
    }

    // The registries a payload is written and read with: the server's where there is one (a dedicated server, or a
    // client's own world), else the client's.
    private static RegistryAccess registries() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        }
        RegistryAccess client = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> BarnyardRomanceForgeClient::registryAccess);
        return client != null ? client : RegistryAccess.EMPTY;
    }
}
