package net.modcrafters.minecraft.barnyardromance.advancement;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Lineage;
//? if >=26.1 {
import net.minecraft.advancements.triggers.CriteriaTriggers;
//?} else
//import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public static final Identifier TWO_BY_TWO = Identifier.fromNamespaceAndPath("minecraft", "husbandry/bred_all_animals");

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

    // The player who put either parent in love, before vanilla clears it; the mother's first, as only she is fed
    // into love.
    public static @Nullable UUID breeder(Animal mother, Animal father) {
        ServerPlayer cause = mother.getLoveCause();
        if (cause == null) {
            cause = father.getLoveCause();
        }
        return cause == null ? null : cause.getUUID();
    }

    public static void conceived(Animal mother, @Nullable UUID breeder) {
        try {
            if (breeder != null && mother.level() instanceof ServerLevel level) {
                Grants.grant(level.getServer(), breeder, BUN_IN_THE_OVEN, Grants.CRITERION);
            }
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to grant a {} pregnancy", AnimalSex.speciesId(mother), exception);
        }
    }

    // Young born to a mother, credited to whoever bred her.
    public static void born(Animal mother, List<Animal> babies, @Nullable UUID breeder) {
        try {
            if (breeder == null || babies.isEmpty() || !(mother.level() instanceof ServerLevel level)) {
                return;
            }
            MinecraftServer server = level.getServer();
            Grants.grant(server, breeder, SPECIAL_DELIVERY, Grants.CRITERION);
            Lineage lineage = Services.DATA.get(babies.getFirst()).map(AnimalTraits::lineage).orElse(Lineage.UNKNOWN);
            if (FamilyTree.third(level, lineage)) {
                Grants.grant(server, breeder, FAMILY_TREE, Grants.CRITERION);
            }
            counted(server, breeder, AnimalSex.speciesId(babies.getFirst()), babies.size());
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to grant a {} birth", AnimalSex.speciesId(mother), exception);
        }
    }

    // A chick (or duckling, or poult) out of a fertilized egg, credited to the players nearby: nobody feeds a hen into
    // love. parents is null for a wild egg, which forgot them; only an egg that remembers them is breeding, so only
    // that one counts towards the ladders, Two by Two and Family Tree.
    public static void hatched(ServerLevel level, Animal chick, @Nullable Lineage parents) {
        try {
            for (ServerPlayer player : Grants.nearby(level, chick.position())) {
                Grants.grant(player, WHICH_CAME_FIRST);
                if (parents == null) {
                    continue;
                }
                counted(level.getServer(), player.getUUID(), AnimalSex.speciesId(chick), 1);
                Animal mother = parents.mother() != null && level.getEntity(parents.mother()) instanceof Animal hen ? hen : chick;
                twoByTwo(level, player.getUUID(), mother, chick);
                if (FamilyTree.third(level, parents)) {
                    Grants.grant(player, FAMILY_TREE);
                }
            }
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to grant a {} hatching", AnimalSex.speciesId(chick), exception);
        }
    }

    // Births towards a player's breeding ladder: counted even while they're offline, and each step reached is granted
    // (or held until they log in).
    public static void counted(MinecraftServer server, UUID player, String speciesId, int babies) {
        Optional<String> ladder = Ladders.ladderFor(speciesId);
        if (ladder.isEmpty() || babies <= 0) {
            return;
        }
        CreditData data = CreditData.get(server);
        int before = data.ledger().births(player, ladder.get());
        int after = data.ledger().addBirths(player, ladder.get(), babies);
        data.setDirty();
        for (int step : Ladders.crossed(before, after)) {
            Grants.grant(server, player, Ladders.advancement(ladder.get(), step), Grants.CRITERION);
        }
    }

    // Vanilla's Two by Two, for young the mod brings into the world later than vanilla's breeding would: vanilla's own
    // trigger fires at mating, before there's a child to count. Online, the trigger fires, so any datapack's
    // bred_animals advancements see the child too; offline, Two by Two's criterion for the child's kind is held.
    public static void twoByTwo(ServerLevel level, @Nullable UUID player, Animal parent, Animal child) {
        try {
            if (player == null) {
                return;
            }
            ServerPlayer online = level.getServer().getPlayerList().getPlayer(player);
            if (online != null) {
                CriteriaTriggers.BRED_ANIMALS.trigger(online, parent, parent, child);
            } else {
                Grants.grant(level.getServer(), player, TWO_BY_TWO, EntityType.getKey(child.getType()).toString());
            }
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to count a {} for Two by Two", AnimalSex.speciesId(child), exception);
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
