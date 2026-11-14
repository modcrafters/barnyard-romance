package net.modcrafters.minecraft.barnyardromance.drops;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

// Drops sized by the animal. Only the drop and shearing mixins call into it, so it registers nothing.
public final class DropsFeature implements Feature {
    @Override
    public String name() {
        return "drops";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
    }
}
