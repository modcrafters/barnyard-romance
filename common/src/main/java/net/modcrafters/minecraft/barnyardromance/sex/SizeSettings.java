package net.modcrafters.minecraft.barnyardromance.sex;

public record SizeSettings(double femaleScale, double maleScale, double variation) {
    public double smallest() {
        return femaleScale - variation;
    }

    public double largest() {
        return maleScale + variation;
    }
}
