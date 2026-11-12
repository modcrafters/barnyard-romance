package net.modcrafters.minecraft.barnyardromance.naturalist;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.minecraft.world.entity.animal.Animal;

import java.lang.reflect.Field;

// Holds off the egg timer on Naturalist's ducks. The field is found by name, not @Shadow: a @Shadow on a field a future
// Naturalist renamed would stop the game loading, and this only stops holding the timer and says so once.
public final class NaturalistDucks {
    private static final int HELD_OFF = 6000;
    private static Field eggTime;
    private static boolean broken;

    private NaturalistDucks() {
    }

    public static void holdEggTimer(Animal duck) {
        if (broken) {
            return;
        }
        try {
            if (eggTime == null) {
                eggTime = duck.getClass().getField("eggTime");
            }
            eggTime.setInt(duck, HELD_OFF);
        } catch (ReflectiveOperationException | IllegalArgumentException exception) {
            broken = true;
            Constants.LOG.warn("Can't hold off the egg timer on Naturalist's ducks, so they may lay extra eggs", exception);
        }
    }
}
