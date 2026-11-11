package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

public final class MilkFeature implements Feature {
    @Override
    public String name() {
        return "milk";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.slice(MilkSlices.LACTATION);
    }
}
