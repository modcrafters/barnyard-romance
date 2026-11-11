package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.Upkeep;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.rivalry.Reach;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// Finding a feeding block's herd (FeederHerd) again: one path search for each animal in reach, so it's only done now
// and then. Animals that join are marked as in a herd, and those that leave every loaded herd as not.
final class HerdRefresh {
    private HerdRefresh() {
    }

    static void refresh(FeederBlockEntity feeder, ServerLevel level) {
        List<UUID> before = List.copyOf(feeder.herd());
        feeder.setHerd(find(level, feeder.getBlockPos(), feeder.kind(), Set.copyOf(before)));
        for (UUID id : feeder.herd()) {
            mark(level, id, true);
        }
        for (UUID id : before) {
            if (!feeder.herdHolds(id) && FeederIndex.feedersOf(id).isEmpty()) {
                mark(level, id, false);
            }
        }
    }

    // The animals that eat here now, nearest first: of this block's kind and an enabled species, past nursing,
    // familiar, within reach, and able to walk here, so a fence keeps the next pen's animals out. When the search says
    // nothing (see Reach.Walk), a member stays, and a newcomer joins only if it's close to the block already: a fence
    // between pens stops a search partway, which says no.
    static List<UUID> find(ServerLevel level, BlockPos pos, FeederKind kind, Set<UUID> members) {
        int range = BarnyardConfig.current().tuning().feederRange();
        Vec3 middle = Vec3.atCenterOf(pos);
        List<Animal> near = new ArrayList<>(level.getEntitiesOfClass(Animal.class, new AABB(pos).inflate(range), animal -> eats(animal, kind)));
        near.sort(Comparator.comparingDouble(animal -> animal.distanceToSqr(middle)));
        List<UUID> found = new ArrayList<>();
        for (Animal animal : near) {
            Reach.Walk walk = Reach.walkTo(animal, pos, range);
            if (walk == Reach.Walk.YES || walk == Reach.Walk.UNKNOWN
                    && (members.contains(animal.getUUID()) || EatAtFeederGoal.within(animal, pos, EatAtFeederGoal.CLOSE_ENOUGH))) {
                found.add(animal.getUUID());
            }
        }
        return found;
    }

    // Everything but the walk, which takes a path search and so is asked last.
    static boolean eats(Animal animal, FeederKind kind) {
        if (!animal.isAlive() || animal.isBaby() || FeederKind.of(animal).orElse(null) != kind
                || !BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return false;
        }
        return Services.DATA.get(animal).map(traits -> Familiarity.ready(traits.familiarity())).orElse(false);
    }

    private static void mark(ServerLevel level, UUID id, boolean inHerd) {
        Entity entity = level.getEntity(id);
        if (!(entity instanceof Animal animal)) {
            return;
        }
        Services.DATA.get(animal).ifPresent(traits -> {
            Upkeep upkeep = traits.upkeep();
            if (upkeep.inHerd() == inHerd) {
                return;
            }
            // An old save's upkeep is worked out on its next settled day. A member is familiar, so it would count as
            // cared for then; it does now, rather than lose the mark that tells it's from an old save.
            Upkeep known = upkeep.equals(Upkeep.UNKNOWN) ? Upkeep.NONE.caredFor() : upkeep;
            Services.DATA.set(animal, traits.withUpkeep(known.withInHerd(inHerd)));
        });
    }
}
