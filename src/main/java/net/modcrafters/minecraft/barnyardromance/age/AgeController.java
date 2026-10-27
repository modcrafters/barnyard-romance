package net.modcrafters.minecraft.barnyardromance.age;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public final class AgeController {
    public static final ResourceKey<DamageType> OLD_AGE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "old_age")
    );
    private static final int DEATH_CHECK_INTERVAL = 20;

    private AgeController() {
    }

    // Untracked animals and disabled species only know vanilla's baby and adult.
    public static AgeStage stage(Animal animal) {
        String speciesId = AnimalSex.speciesId(animal);
        Optional<AnimalTraits> traits = BarnyardConfig.current().speciesEnabled(speciesId) ? Services.DATA.get(animal) : Optional.empty();
        if (traits.isEmpty()) {
            return animal.isBaby() ? AgeStage.BABY : AgeStage.ADULT;
        }
        return stage(animal, traits.get());
    }

    public static AgeStage stage(Animal animal, AnimalTraits traits) {
        return Aging.stage(
                animal.isBaby(),
                traits.bornTick(),
                traits.growthTicks(),
                animal.level().getGameTime(),
                BarnyardConfig.current().ageFor(AnimalSex.speciesId(animal))
        );
    }

    public static boolean allowsLove(Animal animal) {
        try {
            return stage(animal) != AgeStage.YOUNG;
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to check the age of {}, leaving vanilla love in place", AnimalSex.speciesId(animal), exception);
            return true;
        }
    }

    public static void serverTick(Animal animal, ServerLevel level) {
        if ((level.getGameTime() + animal.getId()) % DEATH_CHECK_INTERVAL != 0L) {
            return;
        }
        checkDeath(animal, level, level.getGameTime());
    }

    // Returns true when the animal died of old age. `now` is a parameter so game tests can look far ahead.
    public static boolean checkDeath(Animal animal, ServerLevel level, long now) {
        try {
            if (!animal.isAlive() || now < deathAt(animal)) {
                return false;
            }
            dieOfOldAge(animal, level);
            return true;
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to age {}", AnimalSex.speciesId(animal), exception);
            return false;
        }
    }

    public static long deathAt(Animal animal) {
        BarnyardConfig config = BarnyardConfig.current();
        String speciesId = AnimalSex.speciesId(animal);
        if (!config.speciesEnabled(speciesId)) {
            return Long.MAX_VALUE;
        }
        Optional<AnimalTraits> traits = Services.DATA.get(animal);
        if (traits.isEmpty()) {
            return Long.MAX_VALUE;
        }
        long bits = animal.getUUID().getMostSignificantBits() ^ animal.getUUID().getLeastSignificantBits();
        return Aging.deathAt(traits.get().bornTick(), config.ageFor(speciesId), config.tuning().deathSpread(), Aging.spreadRandom(bits));
    }

    private static void dieOfOldAge(Animal animal, ServerLevel level) {
        Optional<Holder.Reference<DamageType>> type = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(OLD_AGE);
        if (type.isPresent()) {
            //? if >=1.21.2 {
            animal.hurtServer(level, new DamageSource(type.get()), Float.MAX_VALUE);
            //?} else
            //animal.hurt(new DamageSource(type.get()), Float.MAX_VALUE);
        }
        if (animal.isAlive()) {
            //? if >=1.21.2 {
            animal.kill(level);
            //?} else
            //animal.kill();
        }
    }
}
