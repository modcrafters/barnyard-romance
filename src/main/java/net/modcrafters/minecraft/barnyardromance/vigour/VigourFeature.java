package net.modcrafters.minecraft.barnyardromance.vigour;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

public final class VigourFeature implements Feature {
    @Override
    public String name() {
        return "vigour";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.slice(VigourSlices.VIGOUR);
    }
}
