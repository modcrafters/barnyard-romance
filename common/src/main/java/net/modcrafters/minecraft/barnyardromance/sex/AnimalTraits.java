package net.modcrafters.minecraft.barnyardromance.sex;

public record AnimalTraits(
        Sex sex,
        double scale,
        double familiarity,
        long lastFedTick,
        int growthTicks,
        long lastBirthTick,
        String lastBirthSpecies,
        long loveUntil,
        long cooldownUntil
) {
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
        this(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, -1L, -1L);
    }

    public AnimalTraits fed(double familiarity, long lastFedTick) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, loveUntil, cooldownUntil);
    }

    public AnimalTraits gaveBirth(long tick, String species) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, tick, species, loveUntil, cooldownUntil);
    }

    // The game tick love mode ends, or -1 when the animal is not in love.
    public AnimalTraits withLove(long until) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, until, cooldownUntil);
    }

    // The game tick the breeding rest ends, or -1 when there is none.
    public AnimalTraits withCooldown(long until) {
        return new AnimalTraits(sex, scale, familiarity, lastFedTick, growthTicks, lastBirthTick, lastBirthSpecies, loveUntil, until);
    }
}
