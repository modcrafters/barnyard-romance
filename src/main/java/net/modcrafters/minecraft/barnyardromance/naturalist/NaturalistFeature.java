package net.modcrafters.minecraft.barnyardromance.naturalist;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.naturalist.api.NaturalistApi;

public final class NaturalistFeature implements Feature {
    @Override
    public String name() {
        return "naturalist";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.provide(NaturalistApi.class, new NaturalistApiImpl());
        // Asked each time config checks a species, never at registration.
        registrar.ignoredByDefault(NaturalistCompat::ignoredByDefault);
    }
}
