package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.age.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Inheritance;
import net.modcrafters.minecraft.barnyardromance.sex.Lineage;
import net.modcrafters.minecraft.barnyardromance.sex.ScaleRoll;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SexRoll;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;

import java.util.List;
import java.util.Optional;

public final class PregnancyController {
    private PregnancyController() {
    }

    public static boolean replaceBirth(Animal self, Animal partner, ServerLevel level) {
        try {
            return startOrRefuse(self, partner, level);
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to start a pregnancy, leaving vanilla breeding in place", exception);
            return false;
        }
    }

    public static void catchUp(Animal animal, ServerLevel level) {
        Optional<Pregnancy> pregnancy = Services.DATA.getPregnancy(animal);
        if (pregnancy.isEmpty() || !animal.isAlive()) {
            return;
        }
        Pregnancy current = pregnancy.get();
        if (!PregnancyClock.ready(level.getGameTime(), current)) {
            return;
        }
        try {
            giveBirth(animal, level, current);
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to birth {}", AnimalSex.speciesId(animal), exception);
        }
        Services.DATA.clearPregnancy(animal);
    }

    private static boolean startOrRefuse(Animal self, Animal partner, ServerLevel level) {
        BarnyardConfig config = BarnyardConfig.current();
        if (!config.speciesEnabled(AnimalSex.speciesId(self))) {
            return false;
        }
        Optional<AnimalTraits> selfTraits = Services.DATA.get(self);
        Optional<AnimalTraits> partnerTraits = Services.DATA.get(partner);
        if (selfTraits.isEmpty() || partnerTraits.isEmpty()) {
            return false;
        }
        if (selfTraits.get().sex() == partnerTraits.get().sex()) {
            self.resetLove();
            partner.resetLove();
            return true;
        }
        if (!config.pregnancyEnabled()) {
            return false;
        }
        Animal mother = selfTraits.get().sex() == Sex.FEMALE ? self : partner;
        Animal father = mother == self ? partner : self;
        AnimalTraits motherTraits = mother == self ? selfTraits.get() : partnerTraits.get();
        AnimalTraits fatherTraits = mother == self ? partnerTraits.get() : selfTraits.get();
        if (Services.DATA.getPregnancy(mother).isEmpty()) {
            if (Fertility.wornOut(motherTraits, config.ageFor(AnimalSex.speciesId(mother)), level.getGameTime())) {
                // Worn out: they mate, show hearts and rest, but nothing is conceived.
                restWithoutPregnancy(self, partner);
                level.broadcastEntityEvent(mother, (byte) 18);
                return true;
            }
            String speciesId = AnimalSex.speciesId(mother);
            double weight = motherTraits.weight();
            int litterSize = (int) LitterRoll.pick(config.litterRange(speciesId), weight, level.getRandom().nextDouble());
            long durationTicks = LitterRoll.pickTicks(config.pregnancyRange(speciesId), weight, level.getRandom().nextDouble());
            Optional<List<CompoundTag>> litter = Litter.conceive(mother, father, level, Math.max(1, litterSize));
            if (litter.isEmpty()) {
                restWithoutPregnancy(self, partner);
                return true;
            }
            Services.DATA.setPregnancy(mother, new Pregnancy(
                    level.getGameTime(),
                    durationTicks,
                    AnimalSex.speciesId(father),
                    fatherTraits.scale(),
                    litterSize,
                    motherTraits.familiarity(),
                    fatherTraits.growthTicks(),
                    fatherTraits.weight(),
                    litter.get()
            ).withFatherId(father.getUUID()));
        }
        self.finalizeSpawnChildFromBreeding(level, partner, null);
        return true;
    }

    // Same as vanilla's cancelled BabyEntitySpawnEvent: no pregnancy, no XP, the usual rest.
    private static void restWithoutPregnancy(Animal self, Animal partner) {
        self.setAge(6000);
        partner.setAge(6000);
        self.resetLove();
        partner.resetLove();
        BreedingStatus.resting(self);
        BreedingStatus.resting(partner);
    }

    private static void giveBirth(Animal mother, ServerLevel level, Pregnancy pregnancy) {
        int count = Math.max(1, pregnancy.litterSize());
        String bornSpecies = null;
        int youngestGrowth = 0;
        for (int i = 0; i < count; i++) {
            try {
                AgeableMob baby = Litter.deliver(mother, level, pregnancy.litter(), i).orElse(null);
                if (baby == null) {
                    Constants.LOG.warn("Skipped a {} birth because no offspring was created", AnimalSex.speciesId(mother));
                    continue;
                }
                baby.setBaby(true);
                //? if >=1.21.5 {
        baby.snapTo(mother.getX(), mother.getY(), mother.getZ(), 0.0F, 0.0F);
        //?} else
        //baby.moveTo(mother.getX(), mother.getY(), mother.getZ(), 0.0F, 0.0F);
                if (baby instanceof Animal child) {
                    int growth = rememberParents(mother, child, pregnancy);
                    youngestGrowth = Math.max(youngestGrowth, growth);
                    baby.setAge(-growth);
                }
                level.addFreshEntityWithPassengers(baby);
                if (bornSpecies == null) {
                    bornSpecies = EntityType.getKey(baby.getType()).toString();
                }
            } catch (RuntimeException exception) {
                Constants.LOG.warn("Failed to birth a {} offspring", AnimalSex.speciesId(mother), exception);
            }
        }
        if (bornSpecies != null) {
            String species = bornSpecies;
            long now = level.getGameTime();
            long youngUntil = now + youngestGrowth;
            BarnyardConfig config = BarnyardConfig.current();
            AgeSettings age = config.ageFor(AnimalSex.speciesId(mother));
            // A birth starts the milk, and it keeps flowing while the young are babies.
            Services.DATA.get(mother).ifPresent(traits -> Services.DATA.set(
                    mother,
                    traits.gaveBirth(now, species)
                            .withFertility(Fertility.afterBirth(Fertility.current(traits, age, now)), now)
                            .withLactation(traits.lactation().afterBirth(now, youngUntil, config.tuning().lactationTicks()))
            ));
        }
        level.broadcastEntityEvent(mother, (byte) 18);
    }

    private static int rememberParents(Animal mother, Animal child, Pregnancy pregnancy) {
        BarnyardConfig config = BarnyardConfig.current();
        SizeSettings size = config.sizeFor(AnimalSex.speciesId(mother));
        AnimalTraits motherTraits = Services.DATA.get(mother).orElse(null);
        double motherScale = motherTraits == null ? size.smallest() : motherTraits.scale();
        int motherGrowth = motherTraits == null ? Growth.wild() : motherTraits.growthTicks();
        double motherWeight = motherTraits == null ? 0.5 : motherTraits.weight();
        // Superfoods like golden bran feed the litter on top of how well the mother knew her keeper.
        double nutrition = Math.clamp(pregnancy.motherFamiliarity() + pregnancy.care().nutritionBonus(), 0.0, 1.0);
        Sex sex = SexRoll.roll(child.getRandom().nextDouble(), config.femaleRatio());
        double scalePercent = inherit(
                child,
                ScaleRoll.percent(Sex.FEMALE, motherScale, size),
                ScaleRoll.percent(Sex.MALE, pregnancy.fatherScale(), size),
                nutrition
        );
        double scale = ScaleRoll.fromPercent(sex, size, scalePercent);
        int growth = Growth.fromPercent(inherit(child, Growth.percent(motherGrowth), Growth.percent(pregnancy.fatherGrowthTicks()), nutrition));
        double weight = inherit(child, motherWeight, pregnancy.fatherWeight(), nutrition);
        Services.DATA.set(child, new AnimalTraits(sex, scale, Familiarity.inherited(nutrition), -1L, growth)
                .withWeight(weight)
                .withBornTick(mother.level().getGameTime())
                .withLineage(new Lineage(mother.getUUID(), pregnancy.fatherId())));
        return growth;
    }

    private static double inherit(Animal child, double mother, double father, double nutrition) {
        return Inheritance.child(mother, father, child.getRandom().nextDouble(), child.getRandom().nextDouble(), nutrition);
    }
}
