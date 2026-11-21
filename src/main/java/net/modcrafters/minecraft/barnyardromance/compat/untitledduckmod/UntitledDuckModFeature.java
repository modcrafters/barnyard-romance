package net.modcrafters.minecraft.barnyardromance.compat.untitledduckmod;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;

// Untitled Duck Mod's ducks and geese are matched by id, so this does nothing without the mod. They lay by the mod's
// rules (SpeciesPresets), tame through familiarity, and the mod's own egg timer, breeding food and thrown eggs are
// turned off by UntitledDuckEggTimerMixin and UntitledDuckThrownEggMixin.
public final class UntitledDuckModFeature implements Feature {
    public static final String DUCK = "untitledduckmod:duck";
    public static final String GOOSE = "untitledduckmod:goose";

    @Override
    public String name() {
        return "untitled_duck_mod";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.trackedByDefault(DUCK, GOOSE);
    }
}
