package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public final class HerdRecords {
    private HerdRecords() {
    }

    public static Optional<HerdRecord> capture(Animal animal, long now) {
        Optional<AnimalTraits> traits = Services.DATA.get(animal);
        if (traits.isEmpty()) {
            return Optional.empty();
        }
        Pregnancy pregnancy = Services.DATA.getPregnancy(animal).map(HerdRecords::withoutLitter).orElse(null);
        String name = animal.hasCustomName() && animal.getCustomName() != null ? animal.getCustomName().getString() : "";
        return Optional.of(new HerdRecord(AnimalSex.speciesId(animal), name, traits.get(), pregnancy, animal.isBaby(), now, -1L));
    }

    // Below the familiarity threshold the book stops updating an animal: it keeps the old values and only
    // takes the new familiarity, so it can tell when the animal is familiar again.
    public static HerdRecord refreshed(@Nullable HerdRecord old, HerdRecord fresh) {
        if (old == null || Familiarity.ready(fresh.traits().familiarity())) {
            return fresh;
        }
        AnimalTraits kept = old.traits().fed(fresh.traits().familiarity(), old.traits().lastFedTick());
        return new HerdRecord(old.species(), old.name(), kept, old.pregnancy(), old.baby(), old.refreshedAt(), old.diedAt());
    }

    private static Pregnancy withoutLitter(Pregnancy pregnancy) {
        return new Pregnancy(
                pregnancy.conceivedAt(),
                pregnancy.durationTicks(),
                pregnancy.fatherSpecies(),
                pregnancy.fatherScale(),
                pregnancy.litterSize(),
                pregnancy.motherFamiliarity(),
                pregnancy.fatherGrowthTicks(),
                pregnancy.fatherWeight(),
                List.of(),
                pregnancy.care(),
                pregnancy.fatherId()
        );
    }
}
