package net.modcrafters.minecraft.barnyardromance.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

// Every Barnyard Romance advancement has one impossible criterion, done, granted from code when its moment happens.
// A player who is offline then gets it at the next login (see BarnyardAdvancements.loggedIn).
public final class Grants {
    public static final String CRITERION = "done";
    // How close a player must be to see a hatching, a feeder meal or a fight.
    public static final double NEARBY = 16.0;

    private Grants() {
    }

    public static void grant(ServerPlayer player, Identifier advancement) {
        award(player, advancement, CRITERION);
    }

    public static void grant(MinecraftServer server, UUID player, Identifier advancement, String criterion) {
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        if (online != null) {
            award(online, advancement, criterion);
            return;
        }
        CreditData data = CreditData.get(server);
        data.ledger().hold(player, new CreditLedger.Held(advancement.toString(), criterion));
        data.setDirty();
    }

    public static List<ServerPlayer> nearby(ServerLevel level, Vec3 at) {
        return level.players().stream()
                .filter(player -> !player.isSpectator() && player.distanceToSqr(at) <= NEARBY * NEARBY)
                .map(player -> (ServerPlayer) player)
                .toList();
    }

    public static void grantNearby(ServerLevel level, Vec3 at, Identifier advancement) {
        nearby(level, at).forEach(player -> grant(player, advancement));
    }

    // An advancement a datapack removed, or a criterion it renamed, is left alone.
    static void award(ServerPlayer player, Identifier advancement, String criterion) {
        MinecraftServer server = player.level().getServer();
        AdvancementHolder holder = server == null ? null : server.getAdvancements().get(advancement);
        if (holder != null) {
            player.getAdvancements().award(holder, criterion);
        }
    }
}
