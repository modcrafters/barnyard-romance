package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.stats.LiveFacts;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkApi;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyApi;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggsApi;
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
        Pregnancy pregnancy = FeaturesRepository.get(PregnancyApi.class).pregnancy(animal).map(HerdRecords::withoutLitter).orElse(null);
        String name = animal.hasCustomName() && animal.getCustomName() != null ? animal.getCustomName().getString() : "";
        EggsApi eggs = FeaturesRepository.get(EggsApi.class);
        NestCheck nest = eggs.isLayingHen(animal) && animal.level() instanceof ServerLevel level
                ? new NestCheck(eggs.roosterNear(animal, level), eggs.hasBoxWithRoomNear(animal, level))
                : NestCheck.NONE;
        return Optional.of(new HerdRecord(AnimalSex.speciesId(animal), name, traits.get(), pregnancy, animal.isBaby(),
                FeaturesRepository.get(MilkApi.class).givesMilk(animal), animal instanceof Sheep,
                animal instanceof Sheep sheep && !sheep.isSheared(), now, -1L, -1L,
                FamiliarityCare.tamed(animal), nest, DeathCause.NONE,
                new RecordSheet(animal.getHealth(), animal.getMaxHealth(), LiveFacts.needsTaming(animal), eggs.laying(animal).orElse(Laying.NONE))));
    }

    // Below the familiarity threshold the book stops updating an animal: it keeps the old values and only
    // takes the new familiarity, so it can tell when the animal is familiar again.
    public static HerdRecord refreshed(@Nullable HerdRecord old, HerdRecord fresh) {
        if (old == null || Familiarity.ready(fresh.traits().get(FamiliaritySlices.CARE).familiarity())) {
            return fresh;
        }
        // The hunger goes with the familiarity it was settled into, so the book doesn't count those days twice.
        FamiliarityState freshCare = fresh.traits().get(FamiliaritySlices.CARE);
        AnimalTraits kept = old.traits().with(FamiliaritySlices.CARE, old.traits().get(FamiliaritySlices.CARE).withFamiliarity(freshCare.familiarity())
                .withHunger(freshCare.hunger()).withOrigin(freshCare.origin()));
        return new HerdRecord(old.species(), old.name(), kept, old.pregnancy(), old.baby(), old.milkable(), old.shearable(), old.woolly(), old.refreshedAt(),
                old.diedAt(), old.diedClock(), fresh.tamed(), old.nest(), old.cause(), old.sheet());
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
