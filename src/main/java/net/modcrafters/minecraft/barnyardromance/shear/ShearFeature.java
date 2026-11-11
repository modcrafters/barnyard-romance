package net.modcrafters.minecraft.barnyardromance.shear;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

public final class ShearFeature implements Feature {
    @Override
    public String name() {
        return "shear";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.slice(ShearSlices.LAST_SHEARED);
    }
}
