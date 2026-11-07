package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.HungerSettings;
import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.vigour.Vigour;
import net.modcrafters.minecraft.barnyardromance.vigour.VigourRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.Optional;

// Looking after an animal day by day: hungry days cost familiarity and health (see DailyUpkeep), and a tamed animal
// that has lost too much is grumpy rather than wild. Settling runs every server tick but only changes anything after
// midnight.
public final class FamiliarityCare {
    // A pet that has just been tamed can miss a week of meals before it turns grumpy.
    public static final int TAMING_GRACE_DAYS = 7;

    private FamiliarityCare() {
    }

    public static void serverTick(Animal animal, ServerLevel level) {
        try {
            settle(animal, level.getOverworldClockTime());
            if (animal instanceof TamableAnimal pet) {
                Sulk.tick(pet, level.getGameTime());
            }
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to settle a {}'s hunger", AnimalSex.speciesId(animal), exception);
        }
    }

    public static Optional<AnimalTraits> settle(Animal animal) {
        return settle(animal, animal.level().getOverworldClockTime());
    }

    // Applies the days up to yesterday and returns the traits as they are now. clock is an overworld clock time. Each
    // counted day goes through DailyUpkeep: a hungry day costs familiarity, and a familiar animal health as well.
    public static Optional<AnimalTraits> settle(Animal animal, long clock) {
        Optional<AnimalTraits> tracked = tracked(animal);
        if (tracked.isEmpty() || !(animal.level() instanceof ServerLevel level)) {
            return tracked;
        }
        AnimalTraits traits = tracked.get();
        long today = GameDay.of(clock);
        // This runs every tick for every tracked animal; on a day already settled there's nothing to do or make.
        if (traits.hunger().settledDay() >= today && traits.vigour().day() >= today) {
            return tracked;
        }
        // The herd waiting rule goes here: an animal its feeder will settle returns before anything is settled.
        Hunger.Settled settled = Hunger.settle(traits.hunger(), today, animal.isBaby());
        // Hunger settling never moves the last meal, so Vigour is settled against the same one.
        Vigour vigour = VigourRules.settle(traits.vigour(), traits.hunger().lastAteDay(), today);
        if (settled.hunger().equals(traits.hunger())) {
            if (vigour.equals(traits.vigour())) {
                return tracked;
            }
            AnimalTraits refilled = traits.withVigour(vigour);
            Services.DATA.set(animal, refilled);
            return Optional.of(refilled);
        }
        BarnyardConfig config = BarnyardConfig.current();
        HungerSettings hunger = config.hunger();
        Tuning tuning = config.tuning();
        // Nothing is counted the first time, as in Hunger, and the config may count fewer days than were missed.
        long days = settled.first() ? 0L : DailyUpkeep.daysToSettle(today - traits.hunger().settledDay(), hunger.catchUp());
        long hungryDays = Math.min(settled.hungryDays(), days);
        // Days the config doesn't count leave no hungry day behind for Vigour, eggs or milk to see.
        Hunger hungerNow = hungryDays > 0L ? settled.hunger()
                : new Hunger(today, traits.hunger().lastAteDay(), traits.hunger().lastHungryDay());

        Upkeep upkeep = traits.upkeep();
        // Herd book animals from saves before upkeep was kept have been looked after, however low they are now.
        boolean cared = upkeep.cared() || Familiarity.ready(traits.familiarity())
                || upkeep.equals(Upkeep.NONE) && HerdBooks.listed(level.getServer(), animal.getUUID());
        boolean grown = grown(animal);
        boolean isPet = pet(animal);
        double familiarity = traits.familiarity();
        float damage = 0f;
        boolean escaped = false;
        // A meal is always on the first unsettled day (settling runs before every meal), so fed days come first.
        for (long day = 0L; day < days; day++) {
            boolean ate = day < days - hungryDays;
            double roll = ate ? 1.0 : animal.getRandom().nextDouble();
            DailyUpkeep.Outcome outcome = DailyUpkeep.day(
                    new DailyUpkeep.State(familiarity, animal.getHealth() - damage, cared, grown, isPet), ate, roll, hunger, tuning);
            familiarity = outcome.familiarity();
            damage += outcome.damage();
            escaped |= outcome.escapes();
            cared = outcome.cared();
        }

        AnimalTraits updated = traits.fed(familiarity, traits.lastFedTick()).withHunger(hungerNow).withVigour(vigour);
        // A pet tamed before hunger was kept gets the same grace as a newly tamed one.
        if (settled.first() && tamed(animal)) {
            updated = toTamingGrace(animal, updated);
        }
        if ((cared || Familiarity.ready(updated.familiarity())) && !upkeep.cared()) {
            updated = updated.withUpkeep(upkeep.caredFor());
        }
        Services.DATA.set(animal, updated);
        HungerDamage.hurt(animal, level, damage);
        if (escaped && animal.isAlive()) {
            Escapes.runAway(animal, level, false);
        }
        // A pet that has just turned grumpy stops obeying, so one left sitting gets up and wanders off.
        if (animal instanceof TamableAnimal pet && pet.isOrderedToSit() && grumpy(animal)) {
            pet.setOrderedToSit(false);
            pet.setInSittingPose(false);
        }
        return escaped ? Services.DATA.get(animal) : Optional.of(updated);
    }

    // Any food counts: a normal feed, a hurt animal's heal-only meal, and a superfood.
    public static void markEaten(Animal animal) {
        markEaten(animal, animal.level().getOverworldClockTime());
    }

    public static void markEaten(Animal animal, long clock) {
        settle(animal, clock).ifPresent(traits -> {
            if (!animal.level().isClientSide()) {
                long today = GameDay.of(clock);
                // A male who missed yesterday is filled by his first meal today.
                Vigour vigour = VigourRules.onMeal(traits.vigour(), traits.hunger().lastAteDay(), today);
                Services.DATA.set(animal, traits.withHunger(traits.hunger().ate(today)).withVigour(vigour));
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

    // However an animal is tamed (familiarity, riding a horse, another mod), it starts out a week's hunger above the
    // threshold.
    public static void raiseToTamingGrace(Animal animal) {
        tracked(animal).ifPresent(traits -> Services.DATA.set(animal, toTamingGrace(animal, traits)));
    }

    // The grace never takes a wild-caught pet past its cap, and never lowers a pet already above it.
    private static AnimalTraits toTamingGrace(Animal animal, AnimalTraits traits) {
        double grace = WildCaught.tamingGrace(tamingGrace(), traits.origin(), grown(animal), BarnyardConfig.current().tuning());
        return traits.familiarity() < grace ? raise(animal, traits, grace, traits.lastFedTick()) : traits;
    }

    // Every familiarity gain goes through here, so an animal won over as an adult stays a little wild (see WildCaught).
    public static AnimalTraits raise(Animal animal, AnimalTraits traits, double familiarity, long lastFedTick) {
        return WildCaught.raise(traits, familiarity, lastFedTick, grown(animal), pet(animal), BarnyardConfig.current().tuning());
    }

    public static double cap(Animal animal, AnimalTraits traits) {
        return WildCaught.cap(traits.origin(), pet(animal), BarnyardConfig.current().tuning());
    }

    // The animals familiarity tames, tamed yet or not: they live close to you, so they get the higher wild cap.
    public static boolean pet(Animal animal) {
        return animal instanceof Wolf || animal instanceof Cat || animal instanceof AbstractHorse;
    }

    public static boolean grown(Animal animal) {
        AgeStage stage = AgeController.stage(animal);
        return stage == AgeStage.ADULT || stage == AgeStage.OLD;
    }

    public static double tamingGrace() {
        return Math.min(1.0, Familiarity.threshold() + TAMING_GRACE_DAYS * BarnyardConfig.current().tuning().familiarityHungerLoss());
    }

    // Only the animals familiarity tames (wolves, cats, horses and the rest of the horse family) count: a parrot tamed
    // with seeds, or another mod's pet, can't be fed back up, so it never turns grumpy.
    public static boolean tamed(Animal animal) {
        if (animal instanceof Wolf || animal instanceof Cat) {
            return ((TamableAnimal) animal).isTame();
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
