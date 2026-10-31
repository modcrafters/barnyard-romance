package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.milk.MilkActions;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
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
        NestCheck nest = EggLaying.isLayingHen(animal) && animal.level() instanceof ServerLevel level
                ? new NestCheck(EggLaying.roosterNear(animal, level), EggLaying.boxWithRoomNear(animal, level))
                : NestCheck.NONE;
        return Optional.of(new HerdRecord(AnimalSex.speciesId(animal), name, traits.get(), pregnancy, animal.isBaby(),
                MilkActions.milkFor(animal).isPresent(), animal instanceof Sheep, animal instanceof Sheep sheep && !sheep.isSheared(), now, -1L, -1L,
                FamiliarityCare.tamed(animal), nest));
    }

    // Below the familiarity threshold the book stops updating an animal: it keeps the old values and only
    // takes the new familiarity, so it can tell when the animal is familiar again.
    public static HerdRecord refreshed(@Nullable HerdRecord old, HerdRecord fresh) {
        if (old == null || Familiarity.ready(fresh.traits().familiarity())) {
            return fresh;
        }
        // The hunger goes with the familiarity it was settled into, so the book doesn't count those days twice.
        AnimalTraits kept = old.traits().fed(fresh.traits().familiarity(), old.traits().lastFedTick()).withHunger(fresh.traits().hunger())
                .withOrigin(fresh.traits().origin());
        return new HerdRecord(old.species(), old.name(), kept, old.pregnancy(), old.baby(), old.milkable(), old.shearable(), old.woolly(), old.refreshedAt(),
                old.diedAt(), old.diedClock(), fresh.tamed(), old.nest());
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
