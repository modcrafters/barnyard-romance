package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.config.HungerSettings;
import net.modcrafters.minecraft.barnyardromance.core.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.event.Events;
import net.modcrafters.minecraft.barnyardromance.feed.FeederIndex;
import net.modcrafters.minecraft.barnyardromance.herd.api.HerdApi;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.vigour.Vigour;
import net.modcrafters.minecraft.barnyardromance.vigour.VigourRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// Looking after an animal day by day: hungry days cost familiarity and health (see DailyUpkeep), and a tamed animal
// that has lost too much is grumpy rather than wild. Settling runs every server tick but only changes anything after
// midnight.
public final class FamiliarityCare {
    // A pet that has just been tamed can miss a week of meals before it turns grumpy.
    public static final int TAMING_GRACE_DAYS = 7;
    // How long an animal in a herd waits after it loads for a block holding it to load too.
    public static final int HERD_WAIT_TICKS = 100;

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
        // An animal in a herd leaves its missed days to its feeding block, which shares its food over them (see
        // FeederCatchUp). If no block holding it is loaded, it gives one a moment to load, then settles alone.
        if (traits.hunger().settledDay() < today && waitsForHerd(animal, traits, today)) {
            return tracked;
        }
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

        Upkeep upkeep = upkeep(traits);
        boolean cared = caredFor(animal, level, traits, upkeep);
        boolean grown = grown(animal);
        boolean neverRuns = neverRunsAway(animal);
        double familiarity = traits.familiarity();
        float damage = 0f;
        boolean escaped = false;
        // A meal is always on the first unsettled day (settling runs before every meal), so fed days come first.
        for (long day = 0L; day < days; day++) {
            boolean ate = day < days - hungryDays;
            double roll = ate ? 1.0 : animal.getRandom().nextDouble();
            DailyUpkeep.Outcome outcome = DailyUpkeep.day(
                    new DailyUpkeep.State(familiarity, animal.getHealth() - damage, cared, grown, neverRuns), ate, roll, hunger, tuning);
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
        updated = withCare(updated, upkeep, cared);
        Services.DATA.set(animal, updated);
        Events.post(new AnimalEvent.FamiliarityFell(animal, traits.familiarity(), updated.familiarity()));
        toll(animal, level, damage, escaped, days > 1L);
        return escaped ? Services.DATA.get(animal) : Optional.of(updated);
    }

    // One day of a herd's catch-up for an animal settled up to that day (see FeederCatchUp), by the same rules as a
    // lone settle. feeder is the kind of block that fed it that day, if one did. The damage isn't dealt here: the
    // caller adds up a few days' and deals them with toll, and owed, what it owes so far, comes off today's health.
    public static DailyUpkeep.Outcome settleDay(Animal animal, ServerLevel level, AnimalTraits traits, long day, boolean ate,
                                                @Nullable String feeder, float owed) {
        BarnyardConfig config = BarnyardConfig.current();
        Upkeep upkeep = upkeep(traits);
        boolean cared = caredFor(animal, level, traits, upkeep);
        double roll = ate ? 1.0 : animal.getRandom().nextDouble();
        DailyUpkeep.Outcome outcome = DailyUpkeep.day(
                new DailyUpkeep.State(traits.familiarity(), animal.getHealth() - owed, cared, grown(animal), neverRunsAway(animal)),
                ate, roll, config.hunger(), config.tuning());
        Hunger hunger = traits.hunger();
        // A meal or a hungry day marked later, such as a hand meal today while it waited, stays the latest.
        Hunger next = ate ? new Hunger(day + 1L, Math.max(hunger.lastAteDay(), day), hunger.lastHungryDay())
                : new Hunger(day + 1L, hunger.lastAteDay(), Math.max(hunger.lastHungryDay(), day));
        if (feeder != null && upkeep.feederDay() < day) {
            upkeep = upkeep.fedBy(feeder, day);
        }
        AnimalTraits updated = withCare(traits.fed(outcome.familiarity(), traits.lastFedTick()).withHunger(next), upkeep, outcome.cared());
        Services.DATA.set(animal, updated);
        Events.post(new AnimalEvent.FamiliarityFell(animal, traits.familiarity(), updated.familiarity()));
        return outcome;
    }

    // What settled days leave behind: the hunger damage, then the runaway if it's still alive to run. A pet that has
    // just turned grumpy stops obeying, so one left sitting gets up and wanders off. catchingUp: see Escapes.runAway.
    public static void toll(Animal animal, ServerLevel level, float damage, boolean escaped, boolean catchingUp) {
        HungerDamage.hurt(animal, level, damage);
        if (escaped && animal.isAlive()) {
            Escapes.runAway(animal, level, catchingUp);
        }
        if (animal instanceof TamableAnimal pet && pet.isOrderedToSit() && grumpy(animal)) {
            pet.setOrderedToSit(false);
            pet.setInSittingPose(false);
        }
    }

    // It waits while a loaded block holding it has yet to settle the days before today. With no block holding it
    // loaded, it waits HERD_WAIT_TICKS after it loaded, since the block's chunk may load just after its own.
    private static boolean waitsForHerd(Animal animal, AnimalTraits traits, long today) {
        if (!traits.upkeep().inHerd()) {
            return false;
        }
        if (FeederIndex.waiting(animal, today)) {
            return true;
        }
        // tickCount isn't saved, so it counts the ticks since this animal loaded.
        return animal.tickCount < HERD_WAIT_TICKS && !FeederIndex.heldByRunning(animal);
    }

    // Saves from before upkeep was kept start from none.
    private static Upkeep upkeep(AnimalTraits traits) {
        return traits.upkeep().equals(Upkeep.UNKNOWN) ? Upkeep.NONE : traits.upkeep();
    }

    // Looked after so far: marked so, familiar now, or, in a save from before upkeep was kept, listed in a herd
    // book, however low it is now.
    private static boolean caredFor(Animal animal, ServerLevel level, AnimalTraits traits, Upkeep upkeep) {
        boolean oldSave = traits.upkeep().equals(Upkeep.UNKNOWN);
        return upkeep.cared() || Familiarity.ready(traits.familiarity())
                || oldSave && FeaturesRepository.get(HerdApi.class).listed(level.getServer(), animal.getUUID());
    }

    // Marked cared for the first time it is, and otherwise left as it was.
    private static AnimalTraits withCare(AnimalTraits updated, Upkeep upkeep, boolean cared) {
        boolean caredNow = cared || Familiarity.ready(updated.familiarity());
        return updated.withUpkeep(caredNow && !upkeep.cared() ? upkeep.caredFor() : upkeep);
    }

    // True while the animal still has days before today to settle, which happens while it waits for its herd's block
    // (see settle). Whatever is worked out once a day from yesterday's hunger (eggs, milk, Vigour) waits for it.
    public static boolean unsettled(Animal animal, long clock) {
        return settle(animal, clock).map(traits -> traits.hunger().settledDay() < GameDay.of(clock)).orElse(false);
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

    // A meal from a feeding block (feeder is its FeederKind id): the day's meal like any other, noted as the block's.
    // It never touches the hand-feeding clock (lastFedTick), so a hand meal the same day still builds familiarity.
    public static void markFedBy(Animal animal, long clock, String feeder) {
        markEaten(animal, clock);
        tracked(animal).ifPresent(traits -> {
            if (!animal.level().isClientSide()) {
                Services.DATA.set(animal, withCare(traits, upkeep(traits).fedBy(feeder, GameDay.of(clock)), false));
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

    // Pets never run away: wolves, cats and the horse family, which turn grumpy instead, and any other tamed animal,
    // such as a parrot or another mod's pet.
    public static boolean neverRunsAway(Animal animal) {
        return pet(animal) || animal instanceof TamableAnimal tamable && tamable.isTame();
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

    // The animal's traits, if its species is enabled.
    public static Optional<AnimalTraits> tracked(Animal animal) {
        if (!BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return Optional.empty();
        }
        return Services.DATA.get(animal);
    }
}
