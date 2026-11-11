package net.modcrafters.minecraft.barnyardromance.core;

// One feature's registration: in one place, what it hands to the rest of the mod (see FeatureRegistrar).
// BarnyardFeatures lists them in order.
public interface Feature {
    String name();

    void register(FeatureRegistrar registrar);
}
