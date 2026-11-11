package net.modcrafters.minecraft.barnyardromance.advancement;

import net.modcrafters.minecraft.barnyardromance.herd.HerdBookData;
import net.modcrafters.minecraft.barnyardromance.herd.HerdLedger;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

// Whether a baby is the third generation of a line: its mother or father has a known parent of its own. A parent
// that isn't loaded is looked up in the herd books; one in neither counts as unknown.
public final class FamilyTree {
    private FamilyTree() {
    }

    public static boolean third(ServerLevel level, Lineage baby) {
        return hasParent(level, baby.mother()) || hasParent(level, baby.father());
    }

    private static boolean hasParent(ServerLevel level, @Nullable UUID animal) {
        if (animal == null) {
            return false;
        }
        Entity loaded = level.getEntity(animal);
        if (loaded instanceof Animal parent) {
            Lineage lineage = Services.DATA.get(parent).map(AnimalTraits::lineage).orElse(Lineage.UNKNOWN);
            return lineage.mother() != null || lineage.father() != null;
        }
        HerdLedger.Node node = HerdBookData.get(level.getServer()).ledger().node(animal);
        return node != null && (node.mother() != null || node.father() != null);
    }
}
