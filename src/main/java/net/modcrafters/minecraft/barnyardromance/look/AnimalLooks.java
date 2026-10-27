package net.modcrafters.minecraft.barnyardromance.look;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.ClientConfig;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyClock;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.goat.Goat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// How a tracked animal is drawn, worked out on the client from the traits and pregnancy the server already sends.
public final class AnimalLooks {
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private AnimalLooks() {
    }

    // 1 when nothing should change: the switch is off, or the animal is not tracked or not pregnant.
    public static float bellyWiden(LivingEntity entity) {
        if (!ClientConfig.current().belly() || !(entity instanceof Animal animal)) {
            return 1.0f;
        }
        String speciesId = AnimalSex.speciesId(animal);
        BarnyardConfig config = BarnyardConfig.current();
        if (!config.speciesEnabled(speciesId)) {
            return 1.0f;
        }
        Optional<Pregnancy> pregnancy = Services.DATA.getPregnancy(animal);
        if (pregnancy.isEmpty()) {
            return 1.0f;
        }
        double progress = PregnancyClock.progress(animal.level().getGameTime(), pregnancy.get());
        return (float) Belly.widen(progress, config.bellyFor(speciesId));
    }

    // null leaves the animal exactly as vanilla draws it.
    public static HornDraw horns(LivingEntity entity) {
        if (!ClientConfig.current().horns() || !(entity instanceof Animal animal)) {
            return null;
        }
        String speciesId = AnimalSex.speciesId(animal);
        LoadedHorns horns = HornShapes.get(speciesId);
        BarnyardConfig config = BarnyardConfig.current();
        if (horns == null || !config.speciesEnabled(speciesId)) {
            return null;
        }
        Optional<AnimalTraits> found = Services.DATA.get(animal);
        if (found.isEmpty()) {
            return null;
        }
        AnimalTraits traits = found.get();
        boolean male = traits.sex() == Sex.MALE;
        UUID uuid = animal.getUUID();
        float growth = (float) HornGrowth.size(
                animal.isBaby(),
                traits.bornTick(),
                traits.growthTicks(),
                animal.level().getGameTime(),
                config.ageFor(speciesId)
        );
        if (growth <= 0.0f || (!male && !HornRoll.femaleHorned(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), horns.file().femaleChance()))) {
            return new HornDraw(speciesId, horns, List.of(), growth);
        }
        boolean hasLeft = !(animal instanceof Goat goat) || goat.hasLeftHorn();
        boolean hasRight = !(animal instanceof Goat goat) || goat.hasRightHorn();
        List<LoadedHorns.Baked> parts = new ArrayList<>();
        for (LoadedHorns.Baked baked : male ? horns.male() : horns.female()) {
            if (HornSides.drawn(baked.part().side(), hasLeft, hasRight)) {
                parts.add(baked);
            }
        }
        return new HornDraw(speciesId, horns, parts, growth);
    }

    // A shape file that names a part the model lacks would otherwise log every frame.
    public static void warnOnce(String key, String message, Object... args) {
        if (WARNED.add(key)) {
            Constants.LOG.warn(message, args);
        }
    }
}
