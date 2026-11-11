package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.core.AnimalPhase;
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
        registrar.hook(AnimalPhase.SERVER_AI_STEP, "familiarity.care", FamiliarityCare::serverTick);
        registrar.hook(AnimalPhase.AI_STEP_SERVER, "familiarity.escapes", Escapes::tick);
    }
}
