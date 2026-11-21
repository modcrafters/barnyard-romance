package net.modcrafters.minecraft.barnyardromance.compat.environmental;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

// Environmental's duck and yak are matched by id, so this does nothing without the mod. The duck lays by the barnyard's
// rules (EnvironmentalDuckEggTimerMixin and EnvironmentalThrownEggMixin turn off the mod's own timer and hatching); the
// yak is cattle, milked and sheared once a day by the same rules as the rest. Its deer, reindeer, zebra and the rest
// stay out. EnvironmentalLitterMixin keeps its bigger pig and hoglin litters off animals the barnyard looks after.
public final class EnvironmentalFeature implements Feature {
    public static final String DUCK = "environmental:duck";
    public static final String YAK = "environmental:yak";

    @Override
    public String name() {
        return "environmental";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.trackedByDefault(DUCK, YAK);
    }
}
