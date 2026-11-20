package net.modcrafters.minecraft.barnyardromance.naturalist;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

public final class NaturalistFeature implements Feature {
    @Override
    public String name() {
        return "naturalist";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.trackedByDefault(NaturalistCompat.LIVESTOCK.toArray(String[]::new));
    }
}
