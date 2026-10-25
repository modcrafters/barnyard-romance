package net.modcrafters.minecraft.barnyardromance.familiarity;

public final class ProductGate {
    private ProductGate() {
    }

    public static boolean blocked(boolean speciesEnabled, boolean hasTraits, double familiarity) {
        if (!speciesEnabled || !hasTraits) {
            return false;
        }
        return !Familiarity.ready(familiarity);
    }
}
