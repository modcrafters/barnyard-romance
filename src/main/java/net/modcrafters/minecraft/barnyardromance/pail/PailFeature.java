package net.modcrafters.minecraft.barnyardromance.pail;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

// Pails: buckets that hold a few buckets of one fluid, milk first.
public final class PailFeature implements Feature {
    @Override
    public String name() {
        return "pail";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        // A lambda, so PailItems (and its items) load only once the registries are open.
        registrar.content(sink -> {
            PailComponents.register(sink);
            PailItems.register(sink);
        });
    }
}
