package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

import java.util.Map;
import java.util.WeakHashMap;

// A grumpy pet whose owner is attacked nearby doesn't help: it sits down and watches for a while. Only the pose is
// set, not the sit command, and it is never saved. A pet in a fight of its own keeps fighting, and being hurt ends the
// sulk at once.
public final class Sulk {
    public static final int SULK_TICKS = 100;
    public static final double WATCH_RANGE = 16.0;
    // Server thread only. Weak keys, so a pet that is unloaded mid-sulk is simply forgotten.
    private static final Map<TamableAnimal, Long> UNTIL = new WeakHashMap<>();

    private Sulk() {
    }

    // Called once for each time the owner is hurt.
    public static void ownerHurt(TamableAnimal pet, LivingEntity owner, long now) {
        if (pet.getLastHurtByMob() != null || pet.distanceToSqr(owner) > WATCH_RANGE * WATCH_RANGE) {
            return;
        }
        start(pet, now);
    }

    public static boolean sulking(TamableAnimal pet) {
        return UNTIL.containsKey(pet);
    }

    public static void start(TamableAnimal pet, long now) {
        if (!UNTIL.containsKey(pet)) {
            pet.getNavigation().stop();
            pet.setTarget(null);
        }
        pet.setInSittingPose(true);
        UNTIL.put(pet, now + SULK_TICKS);
    }

    public static void tick(TamableAnimal pet, long now) {
        Long until = UNTIL.get(pet);
        if (until == null) {
            return;
        }
        if (now >= until || pet.hurtTime > 0) {
            UNTIL.remove(pet);
            if (!pet.isOrderedToSit()) {
                pet.setInSittingPose(false);
            }
        }
    }
}
