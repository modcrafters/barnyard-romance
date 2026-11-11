package net.modcrafters.minecraft.barnyardromance.breeding;

import net.modcrafters.minecraft.barnyardromance.core.AnimalPhase;
import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

public final class BreedingFeature implements Feature {
    @Override
    public String name() {
        return "breeding";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.hook(AnimalPhase.JOINED, "breeding.traits", (animal, level) -> TraitSetup.rememberAndApply(animal, true));
        registrar.hook(AnimalPhase.AGE_BOUNDARY, "breeding.scale", (animal, level) -> TraitSetup.rememberAndApply(animal, false));
    }
}
