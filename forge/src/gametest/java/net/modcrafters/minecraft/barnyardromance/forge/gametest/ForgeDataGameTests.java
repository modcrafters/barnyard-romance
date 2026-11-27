package net.modcrafters.minecraft.barnyardromance.forge.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.modcrafters.minecraft.barnyardromance.api.Sex;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.data.CoreAttachments;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;
import net.modcrafters.minecraft.barnyardromance.forge.data.AnimalDataSync;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;

// Animal data on Forge 1.20.1, which has no data attachments: the mod keeps it in a capability and sends it to clients
// in its own packet, so these check what NeoForge's attachments do by themselves.
@GameTestHolder(Constants.MOD_ID)
public final class ForgeDataGameTests {
    private static final String PEN = Constants.MOD_ID + ":gametest/pen";
    private static final Vec3 MIDDLE = new Vec3(4.5, 2.0, 4.5);

    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.of(
            "animal_data_survives_save_and_load", ForgeDataGameTests::animalDataSurvivesSaveAndLoad,
            "animal_data_syncs_to_tracking_player", ForgeDataGameTests::animalDataSyncsToTrackingPlayer
    );

    private ForgeDataGameTests() {
    }

    // One batch per test, as the other loaders' game tests run.
    @GameTestGenerator
    public static Collection<TestFunction> generated() {
        return TESTS.entrySet().stream()
                .map(test -> new TestFunction(test.getKey(), Constants.MOD_ID + "." + test.getKey(), PEN, 20, 0L, true, test.getValue()))
                .toList();
    }

    // What the capability writes into the entity's save comes back when a new cow loads it.
    private static void animalDataSurvivesSaveAndLoad(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Animal cow = EntityType.COW.create(level);
        Services.DATA.set(cow, AnimalTraits.of(Sex.FEMALE, 1.2).with(CARE, FamiliarityState.NONE.fed(0.75, 42L)));
        AnimalTraits saved = Services.DATA.get(cow).orElseThrow();

        CompoundTag tag = cow.saveWithoutId(new CompoundTag());
        Animal loaded = EntityType.COW.create(level);
        loaded.load(tag);

        Optional<AnimalTraits> traits = Services.DATA.get(loaded);
        helper.assertTrue(traits.equals(Optional.of(saved)), "the loaded cow is " + traits + ", not " + saved);
        helper.assertTrue(saved.sex() == Sex.FEMALE && saved.scale() == 1.2 && saved.get(CARE).familiarity() == 0.75,
                "the saved cow is " + saved);
        helper.succeed();
    }

    // A player who starts tracking a cow gets its data, and then each change to it, as NeoForge syncs an attachment.
    private static void animalDataSyncsToTrackingPlayer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 middle = helper.absoluteVec(MIDDLE);
        ServerPlayer player = mockPlayer(level);
        player.moveTo(middle.x, middle.y, middle.z);
        level.getChunkSource().move(player);
        List<Packet<?>> sent = SentPackets.capture(player);
        Animal cow = EntityType.COW.create(level);
        try {
            cow.moveTo(middle.x, middle.y, middle.z);
            Services.DATA.set(cow, AnimalTraits.of(Sex.FEMALE, 1.1));
            // The player is right there, so the cow is tracked as soon as it's added.
            level.addFreshEntity(cow);

            AnimalTraits joined = Services.DATA.get(cow).orElseThrow();
            List<AnimalDataSync.Payload> tracked = payloadsFor(sent, cow);
            helper.assertTrue(tracked.size() == 1, "starting to track the cow sent " + tracked.size() + " sync payloads, not 1");
            helper.assertTrue(traitsIn(tracked.get(0)).equals(Optional.of(joined)),
                    "starting to track the cow sent " + traitsIn(tracked.get(0)) + ", not " + joined);

            sent.clear();
            AnimalTraits familiar = joined.with(CARE, FamiliarityState.NONE.fed(0.6, level.getGameTime()));
            Services.DATA.set(cow, familiar);

            List<AnimalDataSync.Payload> changed = payloadsFor(sent, cow);
            helper.assertTrue(changed.size() == 1, "a familiarity change sent " + changed.size() + " sync payloads, not 1");
            helper.assertTrue(changed.get(0).entries().size() == 1, "a familiarity change sent " + changed.get(0).entries());
            helper.assertTrue(traitsIn(changed.get(0)).equals(Optional.of(familiar)),
                    "a familiarity change sent " + traitsIn(changed.get(0)) + ", not " + familiar);
            helper.succeed();
        } finally {
            SentPackets.stop(player);
            cow.discard();
            level.getServer().getPlayerList().remove(player);
        }
    }

    // A mock player in the level, as GameTestHelper.makeMockServerPlayerInLevel makes, but its connection has a channel
    // (one that keeps what it's sent): Forge 1.20.1 sets up a joining player's channel, and vanilla's mock has none.
    private static ServerPlayer mockPlayer(ServerLevel level) {
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "test-mock-player"));
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player);
        return player;
    }

    private static List<AnimalDataSync.Payload> payloadsFor(List<Packet<?>> sent, Animal animal) {
        return SentPackets.payloads(sent).stream()
                .filter(payload -> payload instanceof AnimalDataSync.Payload sync && sync.entityId() == animal.getId())
                .map(payload -> (AnimalDataSync.Payload) payload)
                .toList();
    }

    private static Optional<Object> traitsIn(AnimalDataSync.Payload payload) {
        return payload.entries().stream()
                .filter(entry -> entry.key() == CoreAttachments.ANIMAL_TRAITS)
                .findFirst()
                .flatMap(entry -> entry.value().map(Object.class::cast));
    }
}
