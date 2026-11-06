package net.modcrafters.minecraft.barnyardromance.rivalry;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.vigour.VigourActions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Only females fall in love. When one does, the males who could have her are looked over and one is chosen; only he
// is put in love (quietly, through setInLoveTime, so he shows no hearts), so vanilla's breed goals, goat brains and
// other mods' goals pair the two as they always have, and canMate (MatingGate) accepts no one else. Two or more
// rivals of a species that fights may fight over her instead (see CourtshipRules). Nothing here is saved: love lasts
// half a minute.
public final class Courtship {
    private static final int CHECK_TICKS = 20;
    private static final long FORGET_TICKS = 6_000L;
    private static final double WALK_OVER_DISTANCE = 6.0;

    private static final class State {
        final long startedAt;
        UUID chosen;
        boolean rolled;
        // Her love time while males fight over her, or -1 when it's counting down as usual.
        int held = -1;

        State(long startedAt) {
            this.startedAt = startedAt;
        }
    }

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();
    // Each chosen male and the female he was chosen for.
    private static final Map<UUID, UUID> CHOSEN_FOR = new ConcurrentHashMap<>();

    private Courtship() {
    }

    // A tracked female has just fallen in love.
    public static void start(Animal female) {
        if (female.level() instanceof ServerLevel level && isTracked(female, Sex.FEMALE)) {
            court(female, level.getRandom().nextDouble());
        }
    }

    // roll decides a fight between rivals (below rivalFightChance means a fight); it's only used once per love.
    public static void court(Animal female, double roll) {
        try {
            long now = female.level().getGameTime();
            forgetOld(now);
            State state = STATES.computeIfAbsent(female.getUUID(), id -> new State(now));
            if (state.chosen != null) {
                return;
            }
            List<Animal> eligible = eligible(female);
            Tuning tuning = BarnyardConfig.current().tuning();
            boolean fights = !state.rolled && BarnyardConfig.current().fightsFor(AnimalSex.speciesId(female));
            CourtshipRules.Outcome outcome = CourtshipRules.decide(eligible.size(), fights, roll, tuning.rivalFightChance());
            if (eligible.size() >= 2) {
                state.rolled = true;
            }
            if (outcome.fight()) {
                fightStarted(female, eligible.get(outcome.fighterA()), eligible.get(outcome.fighterB()));
            }
            if (outcome.mate() >= 0) {
                choose(female, state, eligible.get(outcome.mate()));
            }
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to find a mate for a {}", AnimalSex.speciesId(female), exception);
        }
    }

    // Looks again now and then for a female still waiting, and lets go of one whose love has ended.
    public static void tick(Animal animal, ServerLevel level) {
        State state = STATES.get(animal.getUUID());
        if (state == null) {
            return;
        }
        if (state.held > 0 && animal.isInLove()) {
            animal.setInLoveTime(state.held);
        }
        if ((animal.tickCount + animal.getId()) % CHECK_TICKS != 0) {
            return;
        }
        if (!animal.isInLove() || !animal.isAlive()) {
            end(animal.getUUID());
            return;
        }
        if (state.chosen != null) {
            if (level.getEntity(state.chosen) instanceof Animal male && male.isAlive() && male.isInLove()) {
                walkOver(male, animal);
                return;
            }
            CHOSEN_FOR.remove(state.chosen);
            state.chosen = null;
        }
        court(animal, level.getRandom().nextDouble());
    }

    public static boolean chosen(Animal female, Animal male) {
        State state = STATES.get(female.getUUID());
        return state != null && male.getUUID().equals(state.chosen);
    }

    // Called as a pair breeds, from wherever the breeding happens. Only a chosen pair is charged, and only once.
    public static void mated(Animal self, Animal partner) {
        try {
            Animal female = isTracked(self, Sex.FEMALE) ? self : partner;
            Animal male = female == self ? partner : self;
            if (!chosen(female, male)) {
                return;
            }
            end(female.getUUID());
            Tuning tuning = BarnyardConfig.current().tuning();
            VigourActions.pay(male, tuning.vigourPerMating(), tuning.hungryMatingHealth());
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to charge a {} for mating", AnimalSex.speciesId(self), exception);
        }
    }

    // Vigour is a male's only limit, so he gets no breeding rest; a female rests as she always has.
    public static int restAfterMating(Animal animal, int rest) {
        return isTracked(animal, Sex.MALE) ? 0 : rest;
    }

    // The males who could mate with her now, closest first.
    public static List<Animal> eligible(Animal female) {
        Tuning tuning = BarnyardConfig.current().tuning();
        int range = tuning.rivalRange();
        return female.level().getEntitiesOfClass(Animal.class, female.getBoundingBox().inflate(range),
                        male -> male != female && male.isAlive() && !male.isBaby() && sameKind(female, male))
                .stream()
                .filter(male -> available(female, male, tuning))
                .sorted(Comparator.comparingDouble(male -> male.distanceToSqr(female)))
                .filter(male -> Reach.canWalk(male, female, range))
                .toList();
    }

    // Two rivals fight over her; her love waits for them.
    static void fightStarted(Animal female, Animal a, Animal b) {
        Fights.start(a, b, female.getUUID());
        holdLove(female, true);
    }

    // While held, her love doesn't run out; once let go it counts down from where it stood.
    public static void holdLove(Animal female, boolean held) {
        State state = STATES.get(female.getUUID());
        if (state != null) {
            state.held = held ? Math.max(1, female.getInLoveTime()) : -1;
        }
    }

    // The male who won a fight over her gets her, if she's still waiting and he can still have her.
    public static void offerWinner(Animal female, Animal winner) {
        try {
            State state = STATES.get(female.getUUID());
            if (state == null || state.chosen != null || !female.isInLove()) {
                return;
            }
            Tuning tuning = BarnyardConfig.current().tuning();
            if (sameKind(female, winner) && available(female, winner, tuning) && Reach.canWalk(winner, female, tuning.rivalRange())) {
                choose(female, state, winner);
            }
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to give a {} the winner of a fight", AnimalSex.speciesId(female), exception);
        }
    }

    private static boolean available(Animal female, Animal male, Tuning tuning) {
        Optional<AnimalTraits> traits = trackedTraits(male);
        if (traits.isEmpty() || traits.get().sex() != Sex.MALE || !FamiliarityCare.grown(male) || !Familiarity.ready(traits.get().familiarity())) {
            return false;
        }
        UUID taken = CHOSEN_FOR.get(male.getUUID());
        if (taken != null && !taken.equals(female.getUUID())) {
            return false;
        }
        return !Fights.fighting(male) && VigourActions.canPay(male, tuning.vigourPerMating(), tuning.hungryMatingHealth());
    }

    private static void choose(Animal female, State state, Animal male) {
        state.chosen = male.getUUID();
        CHOSEN_FOR.put(male.getUUID(), female.getUUID());
        male.setInLoveTime(Math.max(1, female.getInLoveTime()));
        walkOver(male, female);
    }

    // Vanilla's breed goals only look about 8 blocks for a partner, and a chosen male can be farther off, so he walks
    // over until they're close enough for the goal to take it from there.
    private static void walkOver(Animal male, Animal female) {
        if (male.distanceToSqr(female) > WALK_OVER_DISTANCE * WALK_OVER_DISTANCE) {
            male.getNavigation().moveTo(female, 1.0);
        }
    }

    private static void end(UUID female) {
        State state = STATES.remove(female);
        if (state != null && state.chosen != null) {
            CHOSEN_FOR.remove(state.chosen, female);
        }
    }

    // Females whose chunk unloaded while they were in love are never ticked again.
    private static void forgetOld(long now) {
        STATES.entrySet().removeIf(entry -> {
            boolean old = entry.getValue().startedAt < now - FORGET_TICKS || entry.getValue().startedAt > now;
            if (old && entry.getValue().chosen != null) {
                CHOSEN_FOR.remove(entry.getValue().chosen, entry.getKey());
            }
            return old;
        });
    }

    // A horse and a donkey of opposite sexes make a mule, as vanilla allows.
    private static boolean sameKind(Animal female, Animal male) {
        if (female.getType() == male.getType()) {
            return true;
        }
        return (female instanceof Horse || female instanceof Donkey) && (male instanceof Horse || male instanceof Donkey);
    }

    private static boolean isTracked(Animal animal, Sex sex) {
        return trackedTraits(animal).map(traits -> traits.sex() == sex).orElse(false);
    }

    private static Optional<AnimalTraits> trackedTraits(Animal animal) {
        if (!BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return Optional.empty();
        }
        return Services.DATA.get(animal);
    }
}
