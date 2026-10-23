package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

public final class AnimalHearts {
    private static final int LOVE_INTERVAL = 10;
    private static final int PREGNANCY_INTERVAL = 40;

    private AnimalHearts() {
    }

    public static void serverTick(Animal animal, ServerLevel level) {
        if (Services.DATA.getPregnancy(animal).isPresent()) {
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
    }

    public static void clientTick(Animal animal) {
        if (animal.level() == null || !animal.level().isClientSide() || Services.DATA.getPregnancy(animal).isEmpty()) {
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
}
