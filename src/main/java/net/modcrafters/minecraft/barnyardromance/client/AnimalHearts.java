package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardParticles;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.api.AgeStage;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Tracked;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancySlices;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

// The client's half of the hearts over an animal; the server's half, love hearts, is pregnancy/PregnancyHearts.
public final class AnimalHearts {
    private static final int PREGNANCY_INTERVAL = 40;
    // About once every 12 seconds, far rarer than love (every 10 ticks) and pregnancy (two every 40).
    private static final int OLD_CHANCE = 240;

    private AnimalHearts() {
    }

    public static void clientTick(Animal animal) {
        if (animal.level() == null || !animal.level().isClientSide()) {
            return;
        }
        // A pregnancy left on an animal whose species was switched off shows nothing, as vanilla shows nothing.
        Optional<AnimalTraits> traits = Tracked.traits(animal);
        if (traits.isEmpty()) {
            return;
        }
        if (Services.DATA.get(animal, PregnancyFeature.PREGNANCY).isEmpty()) {
            oldHeart(animal, traits.get());
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

    private static void oldHeart(Animal animal, AnimalTraits traits) {
        if (animal.getRandom().nextInt(OLD_CHANCE) != 0) {
            return;
        }
        if (traits.get(PregnancySlices.BREEDING).loveUntil() > animal.level().getGameTime() || AgeController.stage(animal, traits) != AgeStage.OLD) {
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
