package net.modcrafters.minecraft.barnyardromance.sex;

public enum SizeClass {
    S,
    M,
    L,
    XL;

    public static SizeClass of(Sex sex, double scale, SizeSettings size) {
        double base = sex == Sex.FEMALE ? size.femaleScale() : size.maleScale();
        double span = size.variation() * 2.0;
        double t = span <= 0.0 ? 1.0 : (scale - (base - size.variation())) / span;
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
