package net.modcrafters.minecraft.barnyardromance.rivalry;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.food.FoodData;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Males fighting each other, by UUID. A fight lasts until one of them is dead, gone or far away, or until a player
// comes near with food while both know people; FightGoal does the fighting. Fights and truces live only while their
// animals are loaded, so nothing is saved. A fight over a female holds her love until it's over (see Courtship).
public final class Fights {
    public static final ResourceKey<DamageType> RIVAL = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "rival")
    );
    private static final int STOP_CHECK_TICKS = 10;

    private record Fight(UUID a, UUID b, @Nullable UUID female) {
        UUID other(UUID self) {
            return self.equals(a) ? b : a;
        }
    }

    private static final Map<UUID, Fight> FIGHTS = new ConcurrentHashMap<>();
    // A pair's truce, keyed by both UUIDs in order, and the game tick it ends.
    private static final Map<String, Long> TRUCES = new ConcurrentHashMap<>();

    private Fights() {
    }

    public static void start(Animal a, Animal b, @Nullable UUID female) {
        Fight fight = new Fight(a.getUUID(), b.getUUID(), female);
        FIGHTS.put(a.getUUID(), fight);
        FIGHTS.put(b.getUUID(), fight);
    }

    public static boolean fighting(Animal animal) {
        return FIGHTS.containsKey(animal.getUUID());
    }

    public static Optional<Animal> opponent(Animal animal) {
        Fight fight = FIGHTS.get(animal.getUUID());
        if (fight == null || !(animal.level() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        return level.getEntity(fight.other(animal.getUUID())) instanceof Animal other && other.isAlive() ? Optional.of(other) : Optional.empty();
    }

    public static boolean inTruce(Animal a, Animal b, long now) {
        Long until = TRUCES.get(pair(a.getUUID(), b.getUUID()));
        return until != null && until > now;
    }

    public static boolean killedByRival(Animal animal) {
        DamageSource source = animal.getLastDamageSource();
        return source != null && source.is(RIVAL);
    }

    public static Optional<DamageSource> damage(ServerLevel level) {
        Optional<Holder.Reference<DamageType>> type = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(RIVAL);
        return type.map(DamageSource::new);
    }

    // Ends the fight when the other male is dead, gone or far off, and lets a player with food in hand stop one between
    // two familiar males. Both fighters run this; only the first to see the end acts on it.
    public static void tick(Animal animal, ServerLevel level) {
        Fight fight = FIGHTS.get(animal.getUUID());
        if (fight == null) {
            return;
        }
        try {
            Animal other = level.getEntity(fight.other(animal.getUUID())) instanceof Animal found ? found : null;
            int range = BarnyardConfig.current().tuning().rivalRange();
            if (!animal.isAlive()) {
                end(fight, level, other != null && other.isAlive() ? other : null);
                return;
            }
            if (other == null || !other.isAlive() || other.distanceToSqr(animal) > 4.0 * range * range) {
                end(fight, level, other != null && other.isDeadOrDying() ? animal : null);
                return;
            }
            if (animal.getUUID().compareTo(other.getUUID()) < 0 && (animal.tickCount + animal.getId()) % STOP_CHECK_TICKS == 0
                    && FightRules.stops(familiar(animal), familiar(other), foodNear(level, animal, other))) {
                end(fight, level, null);
                TRUCES.put(pair(animal.getUUID(), other.getUUID()), level.getGameTime() + BarnyardConfig.current().tuning().fightTruceTicks());
            }
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to run a fight between two {}", AnimalSex.speciesId(animal), exception);
            FIGHTS.remove(fight.a(), fight);
            FIGHTS.remove(fight.b(), fight);
        }
    }

    // winner is the male who killed the other, or null when the fight ended any other way.
    private static void end(Fight fight, ServerLevel level, @Nullable Animal winner) {
        boolean removed = FIGHTS.remove(fight.a(), fight);
        removed |= FIGHTS.remove(fight.b(), fight);
        if (!removed || fight.female() == null || !(level.getEntity(fight.female()) instanceof Animal female)) {
            return;
        }
        Courtship.holdLove(female, false);
        if (winner != null) {
            Courtship.offerWinner(female, winner);
        }
    }

    private static boolean familiar(Animal animal) {
        return Services.DATA.get(animal).map(AnimalTraits::familiarity).map(Familiarity::ready).orElse(false);
    }

    private static boolean foodNear(ServerLevel level, Animal a, Animal b) {
        double range = BarnyardConfig.current().tuning().fightStopRange();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || (player.distanceToSqr(a) > range * range && player.distanceToSqr(b) > range * range)) {
                continue;
            }
            if (FoodData.eats(a, player.getMainHandItem()) || FoodData.eats(a, player.getOffhandItem())
                    || FoodData.eats(b, player.getMainHandItem()) || FoodData.eats(b, player.getOffhandItem())) {
                return true;
            }
        }
        return false;
    }

    private static String pair(UUID a, UUID b) {
        return a.compareTo(b) < 0 ? a + "/" + b : b + "/" + a;
    }
}
