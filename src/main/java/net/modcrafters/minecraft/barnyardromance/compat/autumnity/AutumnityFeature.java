package net.modcrafters.minecraft.barnyardromance.compat.autumnity;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

// Autumnity's turkey is matched by id, so this does nothing without the mod. It lays by the barnyard's rules
// (SpeciesPresets), and the mod's own egg timer and thrown-egg hatching are turned off by AutumnityTurkeyEggTimerMixin
// and AutumnityThrownEggMixin. Its snail and other animals stay out.
public final class AutumnityFeature implements Feature {
    public static final String TURKEY = "autumnity:turkey";

    @Override
    public String name() {
        return "autumnity";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.trackedByDefault(TURKEY);
    }
}
