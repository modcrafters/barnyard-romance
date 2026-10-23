package net.modcrafters.minecraft.barnyardromance.sex;

public record AnimalTraits(
        Sex sex,
        double scale,
        double familiarity,
        long lastFedTick,
        int growthTicks,
        long lastBirthTick,
        String lastBirthSpecies
) {
    public AnimalTraits(Sex sex, double scale) {
        this(sex, scale, 0.0, -1L, Growth.DEFAULT);
    }

    public AnimalTraits(Sex sex, double scale, double familiarity, long lastFedTick) {
        this(sex, scale, familiarity, lastFedTick, Growth.DEFAULT);
    }

    public AnimalTraits(Sex sex, double scale, double familiarity, long lastFedTick, int growthTicks) {
        this(sex, scale, familiarity, lastFedTick, growthTicks, -1L, "");
    }

    public AnimalTraits fed(double familiarity, long lastFedTick) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies);
    }

    public AnimalTraits gaveBirth(long tick, String species) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, tick, species);
    }
}
