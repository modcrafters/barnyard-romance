package net.modcrafters.minecraft.barnyardromance.sex;

public record AnimalTraits(Sex sex, double scale, double familiarity, long lastFedTick, int growthTicks) {
    public AnimalTraits(Sex sex, double scale) {
        this(sex, scale, 0.0, -1L, Growth.DEFAULT);
    }

    public AnimalTraits(Sex sex, double scale, double familiarity, long lastFedTick) {
        this(sex, scale, familiarity, lastFedTick, Growth.DEFAULT);
    }

    public AnimalTraits fed(double familiarity, long lastFedTick) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks);
    }
}
