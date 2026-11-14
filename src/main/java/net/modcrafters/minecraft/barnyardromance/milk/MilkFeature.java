package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.core.AnimalPhase;
import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkApi;

public final class MilkFeature implements Feature {
    @Override
    public String name() {
        return "milk";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.provide(MilkApi.class, new MilkApiImpl());
        registrar.slice(MilkSlices.LACTATION);
        registrar.hook(AnimalPhase.JOINED, "milk.joined", (animal, level) -> MilkActions.onJoin(animal));
        // A lambda, so MilkItems (and its items) load only once the registries are open.
        registrar.content(sink -> MilkItems.register(sink));
    }
}
