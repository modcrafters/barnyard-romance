package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.minecraft.world.entity.TamableAnimal;

import java.util.Map;
import java.util.WeakHashMap;

// A grumpy pet whose owner is attacked doesn't help: it sits down and watches for a while. Only the pose is set, not
// the sit command, and it is never saved; being hurt itself ends the sulk at once.
public final class Sulk {
    public static final int SULK_TICKS = 100;
    // Server thread only. Weak keys, so a pet that is unloaded mid-sulk is simply forgotten.
    private static final Map<TamableAnimal, Long> UNTIL = new WeakHashMap<>();

    private Sulk() {
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
