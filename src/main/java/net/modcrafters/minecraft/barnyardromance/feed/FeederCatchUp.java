package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.HungerSettings;
import net.modcrafters.minecraft.barnyardromance.familiarity.DailyUpkeep;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Hunger;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// A feeding block settling the days its herd missed, day by day across the whole herd so the food is shared fairly:
// on the first day every member eats while there's food, then the second day, and so on. When the food runs out
// partway through a day, the members that came first that day ate, so the most it's ever uneven is one day. This is
// HerdCatchUp's rule, applied as it goes so each member's familiarity is read on the day itself. Nobody walks: that's
// taken to have happened. The work is spread over ticks so a big herd never makes a lag spike.
public final class FeederCatchUp {
    // Animal-days settled a tick: a hundred cows' missed week takes three ticks, and no tick much over a millisecond.
    public static final int ANIMAL_DAYS_PER_TICK = 256;
    // A member that has finished, or left the job, is never due again.
    private static final long OUT = Long.MIN_VALUE;

    private final long today;
    // Days up to this one were spent with the block loaded, when the herd could walk over and eat from it, so the
    // block hands out no food for them now.
    private final long liveDay;
    private final List<UUID> members;
    // The day each member settles next.
    private final long[] next;
    // Hunger damage owed this tick, dealt in one blow per animal at the end of it, as a lone settle deals it.
    private final Map<UUID, Float> owed = new LinkedHashMap<>();
    private long day;
    private int at;
    private int left;

    private FeederCatchUp(long today, long liveDay, List<UUID> members, long[] next) {
        this.today = today;
        this.liveDay = liveDay;
        this.members = members;
        this.next = next;
        this.left = members.size();
        long first = today;
        for (long from : next) {
            first = Math.min(first, from);
        }
        this.day = first;
    }

    // Gathers the members loaded now that have days before today to settle. The config may count fewer days than
    // were missed; those it doesn't are passed over at once, as a lone settle passes them over.
    static FeederCatchUp start(FeederBlockEntity feeder, ServerLevel level, long today, long liveDay) {
        HungerSettings.CatchUp mode = BarnyardConfig.current().hunger().catchUp();
        List<UUID> members = new ArrayList<>();
        List<Long> from = new ArrayList<>();
        for (UUID id : feeder.herd()) {
            Animal animal = loaded(level, id);
            AnimalTraits traits = animal == null ? null : FamiliarityCare.tracked(animal).orElse(null);
            // A nursing baby is fed by its mother, so it never goes hungry.
            if (traits == null || animal.isBaby()) {
                continue;
            }
            Hunger hunger = traits.hunger();
            long first = hunger.settledDay();
            // Never settled yet, so its own first settle starts it counting; or settled already, perhaps by another
            // block it's in the herd of too.
            if (first < 0L || first >= today) {
                continue;
            }
            long counted = DailyUpkeep.daysToSettle(today - first, mode);
            if (counted < today - first) {
                first = today - counted;
                Services.DATA.set(animal, traits.withHunger(new Hunger(first, hunger.lastAteDay(), hunger.lastHungryDay())));
                if (counted == 0L) {
                    continue;
                }
            }
            members.add(id);
            from.add(first);
        }
        long[] next = new long[from.size()];
        for (int i = 0; i < next.length; i++) {
            next[i] = from.get(i);
        }
        return new FeederCatchUp(today, liveDay, members, next);
    }

    long today() {
        return today;
    }

    // Settles up to budget animal-days, day first, then member. True once every day before today is done.
    boolean step(FeederBlockEntity feeder, ServerLevel level, int budget) {
        int work = budget;
        while (work > 0 && left > 0 && day < today) {
            if (at >= members.size()) {
                day++;
                at = 0;
                continue;
            }
            int member = at++;
            if (next[member] == day) {
                work--;
                settle(feeder, level, member);
            }
        }
        pay(level);
        return left == 0 || day >= today;
    }

    private void settle(FeederBlockEntity feeder, ServerLevel level, int member) {
        UUID id = members.get(member);
        Animal animal = loaded(level, id);
        AnimalTraits traits = animal == null ? null : FamiliarityCare.tracked(animal).orElse(null);
        // Dead, unloaded, out of the herd, or settled by something else since: it settles alone later, from the day
        // it has reached.
        if (traits == null || animal.isBaby() || !feeder.herdHolds(id) || traits.hunger().settledDay() != day) {
            leave(member);
            return;
        }
        // A meal it had that day before the pen unloaded counts. Otherwise a member still familiar enough eats from
        // the block, on days the pen wasn't loaded to feed itself.
        boolean ate = traits.hunger().lastAteDay() == day;
        boolean fromBlock = false;
        if (!ate && day > liveDay && Familiarity.ready(traits.familiarity())) {
            fromBlock = !feeder.takeOne(level).isEmpty();
            ate = fromBlock;
        }
        float owing = owed.getOrDefault(id, 0f);
        DailyUpkeep.Outcome outcome = FamiliarityCare.settleDay(animal, level, traits, day, ate,
                fromBlock ? feeder.kind().id() : null, owing);
        if (day + 1L >= today) {
            leave(member);
        } else {
            next[member] = day + 1L;
        }
        if (outcome.escapes()) {
            owed.remove(id);
            FamiliarityCare.toll(animal, level, owing + outcome.damage(), true, true);
        } else if (outcome.damage() > 0f) {
            owed.put(id, owing + outcome.damage());
        }
    }

    private void leave(int member) {
        next[member] = OUT;
        left--;
    }

    private void pay(ServerLevel level) {
        for (Map.Entry<UUID, Float> debt : owed.entrySet()) {
            Animal animal = loaded(level, debt.getKey());
            if (animal != null) {
                FamiliarityCare.toll(animal, level, debt.getValue(), false, true);
            }
        }
        owed.clear();
    }

    private static @Nullable Animal loaded(ServerLevel level, UUID id) {
        return level.getEntity(id) instanceof Animal animal && animal.isAlive() ? animal : null;
    }
}
