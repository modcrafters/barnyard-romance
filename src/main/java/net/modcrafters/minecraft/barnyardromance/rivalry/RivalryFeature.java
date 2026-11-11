package net.modcrafters.minecraft.barnyardromance.rivalry;

import net.modcrafters.minecraft.barnyardromance.core.AnimalPhase;
import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

public final class RivalryFeature implements Feature {
    @Override
    public String name() {
        return "rivalry";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.hook(AnimalPhase.AI_STEP_SERVER, "rivalry.courtship", Courtship::tick);
        registrar.hook(AnimalPhase.AI_STEP_SERVER, "rivalry.fights", Fights::tick);
        registrar.hook(AnimalPhase.AI_STEP_SERVER, "rivalry.rival_rolls", RivalRolls::tick);
    }
}
