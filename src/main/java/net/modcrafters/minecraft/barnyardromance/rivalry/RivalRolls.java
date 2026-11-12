package net.modcrafters.minecraft.barnyardromance.rivalry;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.vigour.api.VigourApi;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggsApi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// Fights with no female in sight: roosters that can reach each other now and then, and wild males of other livestock
// rarely. Each grown male rolls once a day at his own time of day (FightRules.rollTime), for every rival with a larger
// UUID, so each pair rolls once. Only loaded animals roll, and missed days are never made up: nobody comes back to
// dead roosters from fights they couldn't have seen.
public final class RivalRolls {
    private static final int CHECK_TICKS = 20;

    private RivalRolls() {
    }

    public static void tick(Animal male, ServerLevel level) {
        if ((male.tickCount + male.getId()) % CHECK_TICKS != 0) {
            return;
        }
        try {
            if (due(male, level.getOverworldClockTime())) {
                roll(male, level, level.getRandom().nextDouble());
            }
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to roll for a fight between two {}", AnimalSex.speciesId(male), exception);
        }
    }

    public static boolean due(Animal male, long clock) {
        if (rolling(male).isEmpty()) {
            return false;
        }
        // The roll reads his familiarity, so his missed days are settled first. A male in a feeding block's herd may
        // be waiting for the block to settle them; he rolls once it has.
        if (FamiliarityCare.unsettled(male, clock)) {
            return false;
        }
        Optional<AnimalTraits> traits = rolling(male);
        return traits.isPresent()
                && FightRules.due(FeaturesRepository.get(VigourApi.class).lastRivalRoll(traits.get()), GameDay.of(clock),
                FeaturesRepository.get(EggsApi.class).ticksSinceMidnight(clock), FightRules.rollTime(male.getUUID()));
    }

    // Rolls today's fights for this male: roll below a pair's chance starts that fight, and at most one starts. The
    // day is used up either way.
    public static void roll(Animal male, ServerLevel level, double roll) {
        Optional<AnimalTraits> traits = rolling(male);
        if (traits.isEmpty()) {
            return;
        }
        long today = GameDay.of(level.getOverworldClockTime());
        Services.DATA.set(male, FeaturesRepository.get(VigourApi.class).rolledForRivals(traits.get(), today));
        if (Fights.fighting(male)) {
            return;
        }
        Tuning tuning = BarnyardConfig.current().tuning();
        boolean rooster = BarnyardConfig.current().eggsFor(AnimalSex.speciesId(male)).enabled();
        boolean wild = !Familiarity.ready(traits.get().get(FamiliaritySlices.CARE).familiarity());
        long now = level.getGameTime();
        for (Animal rival : rivals(male, tuning.matingRange())) {
            Optional<AnimalTraits> theirs = Services.DATA.get(rival);
            if (theirs.isEmpty() || Fights.fighting(rival) || Fights.inTruce(male, rival, now)) {
                continue;
            }
            double chance = FightRules.randomChance(rooster, wild, !Familiarity.ready(theirs.get().get(FamiliaritySlices.CARE).familiarity()), tuning);
            if (roll < chance && Reach.canWalk(male, rival, tuning.matingRange())) {
                Fights.start(male, rival, null);
                return;
            }
        }
    }

    // A tracked, grown male of a species that fights.
    private static Optional<AnimalTraits> rolling(Animal male) {
        if (male.isBaby() || !BarnyardConfig.current().fightsFor(AnimalSex.speciesId(male)) || !FamiliarityCare.grown(male)) {
            return Optional.empty();
        }
        return Services.DATA.get(male).filter(traits -> traits.sex() == Sex.MALE);
    }

    // Grown males of his kind with a larger UUID, closest first.
    private static List<Animal> rivals(Animal male, int range) {
        return male.level().getEntitiesOfClass(Animal.class, male.getBoundingBox().inflate(range),
                        other -> other != male && other.getType() == male.getType() && other.isAlive() && !other.isBaby()
                                && male.getUUID().compareTo(other.getUUID()) < 0 && other.distanceToSqr(male) <= (double) range * range)
                .stream()
                .filter(other -> rolling(other).isPresent())
                .sorted(Comparator.comparingDouble(other -> other.distanceToSqr(male)))
                .toList();
    }
}
