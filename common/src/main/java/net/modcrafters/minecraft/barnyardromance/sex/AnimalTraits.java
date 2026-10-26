package net.modcrafters.minecraft.barnyardromance.sex;

import net.modcrafters.minecraft.barnyardromance.milk.Lactation;

public record AnimalTraits(
        Sex sex,
        double scale,
        double familiarity,
        long lastFedTick,
        int growthTicks,
        long lastBirthTick,
        String lastBirthSpecies,
        long loveUntil,
        long cooldownUntil,
        double weight,
        double fertility,
        long fertilityTick,
        long bornTick,
        Lactation lactation
) {
    public AnimalTraits(
            Sex sex,
            double scale,
            double familiarity,
            long lastFedTick,
            int growthTicks,
            long lastBirthTick,
            String lastBirthSpecies,
            long loveUntil,
            long cooldownUntil,
            double weight,
            double fertility,
            long fertilityTick,
            long bornTick
    ) {
        this(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, loveUntil, cooldownUntil, weight, fertility, fertilityTick, bornTick, Lactation.NONE);
    }

    public AnimalTraits(Sex sex, double scale) {
        this(sex, scale, 0.0, -1L, Growth.wild());
    }

    public AnimalTraits(Sex sex, double scale, double familiarity, long lastFedTick) {
        this(sex, scale, familiarity, lastFedTick, Growth.wild());
    }

    public AnimalTraits(Sex sex, double scale, double familiarity, long lastFedTick, int growthTicks) {
        this(sex, scale, familiarity, lastFedTick, growthTicks, -1L, "");
    }

    public AnimalTraits(
            Sex sex,
            double scale,
            double familiarity,
            long lastFedTick,
            int growthTicks,
            long lastBirthTick,
            String lastBirthSpecies
    ) {
        this(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, -1L, -1L, 0.5, 1.0, -1L, -1L);
    }

    public AnimalTraits fed(double familiarity, long lastFedTick) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, loveUntil, cooldownUntil, weight, fertility, fertilityTick, bornTick, lactation);
    }

    public AnimalTraits gaveBirth(long tick, String species) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, tick, species, loveUntil, cooldownUntil, weight, fertility, fertilityTick, bornTick, lactation);
    }

    // The game tick love mode ends, or -1 when the animal is not in love.
    public AnimalTraits withLove(long until) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, until, cooldownUntil, weight, fertility, fertilityTick, bornTick, lactation);
    }

    // The game tick the breeding rest ends, or -1 when there is none.
    public AnimalTraits withCooldown(long until) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, loveUntil, until, weight, fertility, fertilityTick, bornTick, lactation);
    }

    public AnimalTraits withWeight(double weight) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, loveUntil, cooldownUntil, weight, fertility, fertilityTick, bornTick, lactation);
    }

    // Fertility as of the given tick; it recovers from there with the clock (see Fertility).
    public AnimalTraits withFertility(double fertility, long tick) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, loveUntil, cooldownUntil, weight, fertility, tick, bornTick, lactation);
    }

    // The game tick the animal was born, or -1 when unknown; an unknown birth never ages (see Aging).
    public AnimalTraits withBornTick(long tick) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, loveUntil, cooldownUntil, weight, fertility, fertilityTick, tick, lactation);
    }

    public AnimalTraits withLactation(Lactation lactation) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, loveUntil, cooldownUntil, weight, fertility, fertilityTick, bornTick, lactation);
    }
}
