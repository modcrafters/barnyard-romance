package net.modcrafters.minecraft.barnyardromance.compat.meadow;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

// Let's Do Meadow's water buffalo and wooly cow are matched by id, so this does nothing without the mod. Both are cattle
// (SpeciesPresets): they give milk, the males fight, and they eat from a manger. The mod's wooden bucket milks by the
// barnyard's rules instead of its own unlimited milking, because the animal's interact hook answers the bucket first;
// the wooly cow's milk depends on its variant (WoolyCowMilk).
public final class MeadowFeature implements Feature {
    public static final String WATER_BUFFALO = "meadow:water_buffalo";
    public static final String WOOLY_COW = "meadow:wooly_cow";

    @Override
    public String name() {
        return "meadow";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.trackedByDefault(WATER_BUFFALO, WOOLY_COW);
        registrar.milkResult(WOOLY_COW, new WoolyCowMilk());
    }
}
