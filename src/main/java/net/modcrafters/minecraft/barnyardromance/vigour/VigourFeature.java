package net.modcrafters.minecraft.barnyardromance.vigour;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.vigour.api.VigourApi;

public final class VigourFeature implements Feature {
    @Override
    public String name() {
        return "vigour";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.provide(VigourApi.class, new VigourApiImpl());
        registrar.slice(VigourSlices.VIGOUR);
        // A lambda, so VigourItems (and its item) load only once the registries are open.
        registrar.content(sink -> VigourItems.register(sink));
    }
}
