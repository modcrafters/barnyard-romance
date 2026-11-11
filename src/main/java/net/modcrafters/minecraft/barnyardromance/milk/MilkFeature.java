package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.core.AnimalPhase;
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
        registrar.hook(AnimalPhase.JOINED, "milk.joined", (animal, level) -> MilkActions.onJoin(animal));
    }
}
