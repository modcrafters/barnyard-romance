package net.modcrafters.minecraft.barnyardromance.shear;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.shear.api.ShearApi;

public final class ShearFeature implements Feature {
    @Override
    public String name() {
        return "shear";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.provide(ShearApi.class, ShearRule::state);
        registrar.slice(ShearSlices.LAST_SHEARED);
    }
}
