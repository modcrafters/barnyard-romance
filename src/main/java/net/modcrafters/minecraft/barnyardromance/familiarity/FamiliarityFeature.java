package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

public final class FamiliarityFeature implements Feature {
    @Override
    public String name() {
        return "familiarity";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.slice(FamiliaritySlices.CARE);
    }
}
