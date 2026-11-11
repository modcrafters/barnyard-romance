package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardParticles;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancySlices;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public final class AnimalHearts {
    private static final int LOVE_INTERVAL = 10;
    private static final int PREGNANCY_INTERVAL = 40;
    // About once every 12 seconds, far rarer than love (every 10 ticks) and pregnancy (two every 40).
    private static final int OLD_CHANCE = 240;

    private AnimalHearts() {
    }

    public static void serverTick(Animal animal, ServerLevel level) {
        if (Services.DATA.get(animal, PregnancyFeature.PREGNANCY).isPresent()) {
            animal.resetLove();
            return;
        }
        int inLove = animal.getInLoveTime();
        if (inLove <= 0 || inLove % LOVE_INTERVAL != 0) {
            return;
        }
        double xa = animal.getRandom().nextGaussian() * 0.02;
        double ya = animal.getRandom().nextGaussian() * 0.02;
        double za = animal.getRandom().nextGaussian() * 0.02;
        //? if >=1.21.4 {
        level.sendParticles(
                ParticleTypes.HEART,
                animal.getRandomX(1.0),
                animal.getRandomY() + 0.5,
                animal.getRandomZ(1.0),
                1,
                0.0,
                0.0,
                0.0,
                xa,
                ya,
                za
        );
        //?} else {
        /*// A count of 0 sends one particle that moves by the given offsets times the speed.
        level.sendParticles(ParticleTypes.HEART, animal.getRandomX(1.0), animal.getRandomY() + 0.5, animal.getRandomZ(1.0), 0, xa, ya, za, 1.0);
        *///?}
    }

    public static void clientTick(Animal animal) {
        if (animal.level() == null || !animal.level().isClientSide()) {
            return;
        }
        if (Services.DATA.get(animal, PregnancyFeature.PREGNANCY).isEmpty()) {
            oldHeart(animal);
            return;
        }
        animal.resetLove();
        if (animal.level().getGameTime() % PREGNANCY_INTERVAL != 0) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            animal.level().addParticle(
                    BarnyardParticles.PREGNANCY_HEART,
                    animal.getRandomX(1.0),
                    animal.getRandomY() + 0.5,
                    animal.getRandomZ(1.0),
                    0.0,
                    0.0,
                    0.0
            );
        }
    }

    private static void oldHeart(Animal animal) {
        if (animal.getRandom().nextInt(OLD_CHANCE) != 0) {
            return;
        }
        Optional<AnimalTraits> traits = Services.DATA.get(animal);
        if (traits.isEmpty() || traits.get().get(PregnancySlices.BREEDING).loveUntil() > animal.level().getGameTime() || AgeController.stage(animal) != AgeStage.OLD) {
            return;
        }
        animal.level().addParticle(
                BarnyardParticles.OLD_HEART,
                animal.getRandomX(0.6),
                animal.getRandomY() + 0.5,
                animal.getRandomZ(0.6),
                0.0,
                0.0,
                0.0
        );
    }
}
