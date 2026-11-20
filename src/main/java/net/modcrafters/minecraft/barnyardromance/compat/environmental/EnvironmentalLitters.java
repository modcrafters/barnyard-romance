package net.modcrafters.minecraft.barnyardromance.compat.environmental;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;

import java.lang.reflect.Method;

// Environmental adds extra piglets and hoglets to every breeding in a BabyEntitySpawnEvent handler. The barnyard rolls
// litters itself, so the extras are for a species the barnyard doesn't look after. The event is NeoForge's, which the
// shared code can't name, so its parent is read by reflection; a version that renames it keeps Environmental's litters
// and says so once.
public final class EnvironmentalLitters {
    private static volatile Method parentA;
    private static volatile boolean gaveUp;

    private EnvironmentalLitters() {
    }

    // Whether the barnyard sets the litter for this breeding event: its first parent, the mother, is a species the
    // barnyard tracks. A pack that switches the species off gets Environmental's extra babies back.
    public static boolean ownsLitter(Object event) {
        if (gaveUp) {
            return false;
        }
        try {
            Method method = parentA;
            if (method == null) {
                method = event.getClass().getMethod("getParentA");
                parentA = method;
            }
            return method.invoke(event) instanceof Animal mother
                    && BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(mother));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            gaveUp = true;
            Constants.LOG.warn("Can't read the parents of Environmental's breeding event, so its larger litters are left as they are", exception);
            return false;
        }
    }
}
