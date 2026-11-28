package net.modcrafters.minecraft.barnyardromance.forge.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.modcrafters.minecraft.barnyardromance.compat.legacy.CustomPacketPayload;
import net.modcrafters.minecraft.barnyardromance.compat.legacy.RegistryFriendlyByteBuf;
import net.modcrafters.minecraft.barnyardromance.compat.legacy.StreamCodec;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.forge.platform.ForgeNetworkService;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// The client's copy of animal data, kept as NeoForge 21.1 keeps a synced attachment's (AttachmentSync):
// - a change (a key set or removed) goes at once to every player tracking the animal, as that one key;
// - a player who starts tracking the animal gets every key it has. Joining, respawning and changing dimension all start
//   tracking afresh, so they're covered the same way;
// - the client drops its copy with the entity, and the next tracking sends it again.
// NeoForge copies only copy-on-death attachments to a converted animal, which the mod's aren't, so nothing is copied
// here either: the mod carries what it needs itself (Conversions).
public final class AnimalDataSync {
    private AnimalDataSync() {
    }

    public static void register() {
        ForgeNetworkService.toClient(Payload.TYPE, Payload.class, Payload.STREAM_CODEC);
        MinecraftForge.EVENT_BUS.addListener(AnimalDataSync::onStartTracking);
    }

    // One key changed on the server: its value, or its removal, for everyone tracking the animal.
    static void changed(Animal animal, AttachmentKey<?> key) {
        if (!(animal.level() instanceof ServerLevel level) || level.players().isEmpty()) {
            return;
        }
        ForgeAnimalData.Values values = ForgeAnimalData.of(animal);
        if (values == null) {
            return;
        }
        ForgeNetworkService.sendToTrackingAndSelf(animal, new Payload(animal.getId(), List.of(entry(values, key))));
    }

    private static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Animal animal && event.getEntity() instanceof ServerPlayer player) {
            ForgeAnimalData.Values values = ForgeAnimalData.of(animal);
            if (values == null || values.keys().isEmpty()) {
                return;
            }
            List<Entry<?>> entries = new ArrayList<>();
            for (AttachmentKey<?> key : values.keys()) {
                entries.add(entry(values, key));
            }
            Services.NETWORK.sendToPlayer(player, new Payload(animal.getId(), entries));
        }
    }

    // Client side: puts what the server sent on the client's own animal. Called on the client's main thread.
    public static void apply(Payload payload, @Nullable Entity entity) {
        if (!(entity instanceof Animal animal)) {
            return;
        }
        ForgeAnimalData.Values values = ForgeAnimalData.of(animal);
        if (values == null) {
            return;
        }
        for (Entry<?> entry : payload.entries()) {
            entry.applyTo(values);
        }
    }

    private static <T> Entry<T> entry(ForgeAnimalData.Values values, AttachmentKey<T> key) {
        return new Entry<>(key, Optional.ofNullable(values.get(key)));
    }

    // The keys of one animal, by its entity id.
    public record Payload(int entityId, List<Entry<?>> entries) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(new ResourceLocation(Constants.MOD_ID, "animal_data"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeVarInt(payload.entityId());
                    buf.writeVarInt(payload.entries().size());
                    for (Entry<?> entry : payload.entries()) {
                        entry.write(buf);
                    }
                },
                buf -> {
                    int entityId = buf.readVarInt();
                    int count = buf.readVarInt();
                    List<Entry<?>> entries = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        entries.add(Entry.read(buf));
                    }
                    return new Payload(entityId, entries);
                }
        );

        @Override
        public Type<Payload> type() {
            return TYPE;
        }
    }

    // One key's value, or empty when the animal no longer has it.
    public record Entry<T>(AttachmentKey<T> key, Optional<T> value) {
        void write(RegistryFriendlyByteBuf buf) {
            buf.writeUtf(key.id());
            buf.writeBoolean(value.isPresent());
            value.ifPresent(present -> key.streamCodec().encode(buf, present));
        }

        static Entry<?> read(RegistryFriendlyByteBuf buf) {
            String id = buf.readUtf();
            AttachmentKey<?> key = ForgeAnimalData.key(id)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown animal data " + id + " from the server"));
            return read(buf, key);
        }

        private static <T> Entry<T> read(RegistryFriendlyByteBuf buf, AttachmentKey<T> key) {
            return new Entry<>(key, buf.readBoolean() ? Optional.of(key.streamCodec().decode(buf)) : Optional.empty());
        }

        void applyTo(ForgeAnimalData.Values values) {
            value.ifPresentOrElse(present -> values.set(key, present), () -> values.remove(key));
        }
    }
}
