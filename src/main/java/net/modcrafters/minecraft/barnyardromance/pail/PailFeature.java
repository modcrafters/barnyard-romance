package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.core.registries.Registries;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardApi;
import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.pail.api.PailApi;

// Pails: buckets that hold a few buckets of one fluid, milk first.
public final class PailFeature implements Feature {
    @Override
    public String name() {
        return "pail";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.provide(PailApi.class, new PailApiImpl());
        BarnyardApi.installPails(new PailsApiImpl());
        // A lambda, so PailItems and MilkFluids (and their items and fluids) load only once the registries are open.
        registrar.content(sink -> {
            PailComponents.register(sink);
            PailItems.register(sink);
            MilkFluids.register(sink);
            sink.register(Registries.RECIPE_SERIALIZER, PailRecipe.ID, () -> PailRecipe.SERIALIZER);
        });
    }
}
