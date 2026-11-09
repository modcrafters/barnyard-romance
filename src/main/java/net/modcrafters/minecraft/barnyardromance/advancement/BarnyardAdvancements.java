package net.modcrafters.minecraft.barnyardromance.advancement;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

import java.util.List;

// The moments the Barnyard Romance advancement tab grants, each called from where the mod already handles it.
// A failure here is logged and never gets in the way of what the player was doing.
public final class BarnyardAdvancements {
    public static final Identifier ROOT = id("root");
    public static final Identifier BUN_IN_THE_OVEN = id("bun_in_the_oven");
    public static final Identifier SPECIAL_DELIVERY = id("special_delivery");
    public static final Identifier FAMILY_TREE = id("family_tree");
    public static final Identifier SELF_SERVICE = id("self_service");
    public static final Identifier UDDER_TRUST = id("udder_trust");
    public static final Identifier FLEECE_NAVIDAD = id("fleece_navidad");
    public static final Identifier NOT_YOU_ITS_DINNER = id("not_you_its_dinner");
    public static final Identifier PAPER_TRAIL = id("paper_trail");
    public static final Identifier WHICH_CAME_FIRST = id("which_came_first");
    public static final Identifier LOCKING_HORNS = id("locking_horns");
    public static final Identifier SILENT_TREATMENT = id("silent_treatment");

    private BarnyardAdvancements() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "barnyard/" + path);
    }

    // A hand meal that makes a wild animal familiar.
    public static void befriended(Animal animal, Player player) {
        try {
            if (player instanceof ServerPlayer server) {
                Grants.grant(server, ROOT);
            }
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to grant befriending a {}", AnimalSex.speciesId(animal), exception);
        }
    }

    // Whatever was earned while the player was away.
    public static void loggedIn(ServerPlayer player) {
        try {
            MinecraftServer server = player.level().getServer();
            if (server == null) {
                return;
            }
            CreditData data = CreditData.get(server);
            List<CreditLedger.Held> held = data.ledger().takeHeld(player.getUUID());
            if (held.isEmpty()) {
                return;
            }
            data.setDirty();
            for (CreditLedger.Held grant : held) {
                Identifier id = Identifier.tryParse(grant.advancement());
                if (id != null) {
                    Grants.award(player, id, grant.criterion());
                }
            }
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to give {} their held advancements", player.getScoreboardName(), exception);
        }
    }
}
