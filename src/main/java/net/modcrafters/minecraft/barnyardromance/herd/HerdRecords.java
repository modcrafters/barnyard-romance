package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkApi;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyApi;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.SkippedTime;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggsApi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
                AnimalSex.tamed(animal), nest, DeathCause.NONE,
                new RecordSheet(animal.getHealth(), animal.getMaxHealth(), AnimalSex.needsTaming(animal), eggs.laying(animal).orElse(Laying.NONE))));
    }

    // Below the familiarity threshold the book stops updating an animal: it keeps the old values and only
    // takes the new familiarity, so it can tell when the animal is familiar again.
    public static HerdRecord refreshed(@Nullable HerdRecord old, HerdRecord fresh) {
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        if (old == null || familiarityApi.isFamiliar(fresh.traits())) {
            return fresh;
        }
        AnimalTraits kept = familiarityApi.withFamiliarityFrom(old.traits(), fresh.traits());
        return new HerdRecord(old.species(), old.name(), kept, old.pregnancy(), old.baby(), old.milkable(), old.shearable(), old.woolly(), old.refreshedAt(),
                old.diedAt(), old.diedClock(), fresh.tamed(), old.nest(), old.cause(), old.sheet());
    }

    // A record saved before animals kept their size range is taken to be in the given range for its species, and its
    // pregnancy's father in the one for his. The same record comes back when nothing was missing.
    static HerdRecord withSizeRanges(HerdRecord record, Function<String, SizeSettings> rangeFor) {
        AnimalTraits traits = record.traits().sizeRange().isPresent()
                ? record.traits()
                : record.traits().withSizeRange(rangeFor.apply(record.species()));
        Pregnancy pregnancy = record.pregnancy();
        if (pregnancy != null && pregnancy.fatherSizeRange() == null) {
            pregnancy = pregnancy.withFatherSizeRange(rangeFor.apply(pregnancy.fatherSpecies()));
        }
        if (traits == record.traits() && pregnancy == record.pregnancy()) {
            return record;
        }
        return new HerdRecord(record.species(), record.name(), traits, pregnancy, record.baby(), record.milkable(), record.shearable(),
                record.woolly(), record.refreshedAt(), record.diedAt(), record.diedClock(), record.tamed(), record.nest(), record.cause(), record.sheet());
    }

    // The record keeps the father's range, so his size letter reads against the range his scale was set in.
    static Pregnancy withoutLitter(Pregnancy pregnancy) {
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
                pregnancy.fatherId(),
                SkippedTime.UNSEEN,
                null,
                pregnancy.fatherSizeRange()
        );
    }
}
