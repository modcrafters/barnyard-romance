package net.modcrafters.minecraft.barnyardromance.api;

/**
 * An animal's size in four bands, as the stat card shows it.
 */
public enum SizeClass {
    S,
    M,
    L,
    XL;

    /**
     * scale is the animal's size multiplier; base is its species' average for its sex, and variation how far either
     * side of base sizes go. A scale at or past either edge is S or XL.
     */
    public static SizeClass of(double scale, double base, double variation) {
        double span = variation * 2.0;
        double t = span <= 0.0 ? 1.0 : (scale - (base - variation)) / span;
        t = Math.clamp(t, 0.0, 1.0);
        int index = (int) Math.floor(t * 4.0 + 1.0e-9);
        if (index >= 3) {
            return XL;
        }
        if (index == 2) {
            return L;
        }
        if (index == 1) {
            return M;
        }
        return S;
    }
}
