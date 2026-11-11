package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.event.Events;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// A baby from vanilla breeding (pregnancy turned off) still remembers its parents and joins its mother's books.
// Births through a pregnancy set their own lineage, so a baby whose parents are already known is left alone. The
// breeder earns the pregnancy and birth advancements at once; vanilla already counted the baby for Two by Two.
public final class VanillaBirth {
    private VanillaBirth() {
    }

    public static void born(Animal parent, Animal partner, @Nullable AgeableMob baby, @Nullable UUID breeder) {
        if (!(baby instanceof Animal child) || !(parent.level() instanceof ServerLevel)) {
            return;
        }
        Animal mother;
        try {
            // The baby joins the level after this, so its traits are made now, the way joining would make them.
            AnimalSex.onJoin(child);
            Optional<AnimalTraits> traits = Services.DATA.get(child);
            if (traits.isEmpty() || !traits.get().lineage().equals(Lineage.UNKNOWN)) {
                return;
            }
            mother = female(partner) && !female(parent) ? partner : parent;
            Animal father = mother == parent ? partner : parent;
            Services.DATA.set(child, traits.get().withLineage(new Lineage(mother.getUUID(), father.getUUID())));
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to record a {} birth's parents", AnimalSex.speciesId(parent), exception);
            return;
        }
        Events.post(new AnimalEvent.Born(mother, List.of(child), breeder, AnimalEvent.Birth.VANILLA));
    }

    private static boolean female(Animal animal) {
        return Services.DATA.get(animal).map(traits -> traits.sex() == Sex.FEMALE).orElse(false);
    }
}
