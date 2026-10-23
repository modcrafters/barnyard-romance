package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.OffspringScale;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SexRoll;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;

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
        if (!PregnancyClock.ready(level.getGameTime(), current.conceivedAt(), current.durationTicks())) {
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
            String speciesId = AnimalSex.speciesId(mother);
            int litterSize = config.litterSize(speciesId);
            Services.DATA.setPregnancy(mother, new Pregnancy(
                    level.getGameTime(),
                    config.pregnancyTicks(speciesId),
                    AnimalSex.speciesId(father),
                    fatherTraits.scale(),
                    litterSize,
                    motherTraits.familiarity(),
                    fatherTraits.growthTicks(),
                    Litter.conceive(mother, father, level, Math.max(1, litterSize))
            ));
        }
        self.finalizeSpawnChildFromBreeding(level, partner, null);
        return true;
    }

    private static void giveBirth(Animal mother, ServerLevel level, Pregnancy pregnancy) {
        int count = Math.max(1, pregnancy.litterSize());
        for (int i = 0; i < count; i++) {
            try {
                AgeableMob baby = Litter.deliver(mother, level, pregnancy.litter(), i).orElse(null);
                if (baby == null) {
                    Constants.LOG.warn("Skipped a {} birth because no offspring was created", AnimalSex.speciesId(mother));
                    continue;
                }
                baby.setBaby(true);
                baby.snapTo(mother.getX(), mother.getY(), mother.getZ(), 0.0F, 0.0F);
                if (baby instanceof Animal child) {
                    baby.setAge(-rememberParents(mother, child, pregnancy));
                }
                level.addFreshEntityWithPassengers(baby);
            } catch (RuntimeException exception) {
                Constants.LOG.warn("Failed to birth a {} offspring", AnimalSex.speciesId(mother), exception);
            }
        }
        level.broadcastEntityEvent(mother, (byte) 18);
    }

    private static int rememberParents(Animal mother, Animal child, Pregnancy pregnancy) {
        BarnyardConfig config = BarnyardConfig.current();
        SizeSettings size = config.sizeFor(AnimalSex.speciesId(mother));
        AnimalTraits motherTraits = Services.DATA.get(mother).orElse(null);
        double motherScale = motherTraits == null ? size.smallest() : motherTraits.scale();
        int motherGrowth = motherTraits == null ? Growth.DEFAULT : motherTraits.growthTicks();
        double nutrition = pregnancy.motherFamiliarity();
        Sex sex = SexRoll.roll(child.getRandom().nextDouble(), config.femaleRatio());
        double scale = OffspringScale.childScale(
                sex,
                motherScale,
                pregnancy.fatherScale(),
                size,
                child.getRandom().nextDouble(),
                nutrition
        );
        int growth = Growth.child(motherGrowth, pregnancy.fatherGrowthTicks(), child.getRandom().nextDouble(), nutrition);
        Services.DATA.set(child, new AnimalTraits(sex, scale, Familiarity.inherited(nutrition), -1L, growth));
        return growth;
    }
}
