package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import java.util.Optional;

// Looking after an animal day by day: hungry days cost familiarity (see Hunger), and a tamed animal that has lost
// too much is grumpy rather than wild. Settling runs every server tick but only changes anything after midnight.
public final class FamiliarityCare {
    private FamiliarityCare() {
    }

    public static void serverTick(Animal animal, ServerLevel level) {
        try {
            settle(animal, level.getOverworldClockTime());
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to settle a {}'s hunger", AnimalSex.speciesId(animal), exception);
        }
    }

    public static Optional<AnimalTraits> settle(Animal animal) {
        return settle(animal, animal.level().getOverworldClockTime());
    }

    // Applies the hungry days up to yesterday and returns the traits as they are now. clock is an overworld clock time.
    public static Optional<AnimalTraits> settle(Animal animal, long clock) {
        Optional<AnimalTraits> tracked = tracked(animal);
        if (tracked.isEmpty() || animal.level().isClientSide()) {
            return tracked;
        }
        AnimalTraits traits = tracked.get();
        Hunger.Settled settled = Hunger.settle(traits.hunger(), GameDay.of(clock), animal.isBaby());
        if (settled.hunger().equals(traits.hunger())) {
            return tracked;
        }
        Tuning tuning = BarnyardConfig.current().tuning();
        double familiarity = Hunger.afterHunger(traits.familiarity(), settled.hungryDays(), tuning.familiarityHungerLoss());
        // A pet tamed before hunger was kept starts at the threshold, so it isn't grumpy the day this arrives.
        if (settled.first() && tamed(animal)) {
            familiarity = Math.max(familiarity, Familiarity.threshold());
        }
        AnimalTraits updated = traits.fed(familiarity, traits.lastFedTick()).withHunger(settled.hunger());
        if (settled.hungryDays() > 0) {
            // Each hungry day takes the next day's milk, so it doesn't count towards her drying up.
            long hungryTicks = settled.hungryDays() * GameDay.TICKS;
            long now = animal.level().getGameTime();
            updated = updated.withLactation(updated.lactation().postponed(hungryTicks, tuning.milkKeepsFlowingTicks(), now - hungryTicks));
        }
        Services.DATA.set(animal, updated);
        return Optional.of(updated);
    }

    // Any food counts: a normal feed, a hurt animal's heal-only meal, and a superfood.
    public static void markEaten(Animal animal) {
        markEaten(animal, animal.level().getOverworldClockTime());
    }

    public static void markEaten(Animal animal, long clock) {
        settle(animal, clock).ifPresent(traits -> {
            if (!animal.level().isClientSide()) {
                Services.DATA.set(animal, traits.withHunger(traits.hunger().ate(GameDay.of(clock))));
            }
        });
    }

    // Milking and shearing cost a little familiarity: animals don't much enjoy them.
    public static void spend(Animal animal, double cost) {
        settle(animal).ifPresent(traits -> {
            if (!animal.level().isClientSide() && cost > 0.0) {
                Services.DATA.set(animal, traits.fed(Math.max(0.0, traits.familiarity() - cost), traits.lastFedTick()));
            }
        });
    }

    // However an animal is tamed (familiarity, riding a horse, another mod), it starts out at least familiar.
    public static void raiseToThreshold(Animal animal) {
        tracked(animal).ifPresent(traits -> {
            if (!Familiarity.ready(traits.familiarity())) {
                Services.DATA.set(animal, traits.fed(Familiarity.threshold(), traits.lastFedTick()));
            }
        });
    }

    public static boolean tamed(Animal animal) {
        if (animal instanceof TamableAnimal pet) {
            return pet.isTame();
        }
        return animal instanceof AbstractHorse horse && horse.isTamed();
    }

    // Tamed animals never go wild again; below the threshold they are grumpy instead and won't take orders.
    public static boolean grumpy(Animal animal) {
        return tamed(animal) && tracked(animal).map(traits -> !Familiarity.ready(traits.familiarity())).orElse(false);
    }

    private static Optional<AnimalTraits> tracked(Animal animal) {
        if (!BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return Optional.empty();
        }
        return Services.DATA.get(animal);
    }
}
