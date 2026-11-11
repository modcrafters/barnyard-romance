package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

// The server's half of the hearts over an animal: a pregnant animal isn't in love, and love shows hearts. The client's
// half (pregnancy and old-age hearts) is client/AnimalHearts.
public final class PregnancyHearts {
    private static final int LOVE_INTERVAL = 10;

    private PregnancyHearts() {
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
}
