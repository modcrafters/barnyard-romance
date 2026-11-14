package net.modcrafters.minecraft.barnyardromance.breeding;

import net.minecraft.util.RandomSource;
import net.modcrafters.minecraft.barnyardromance.breeding.api.BreedingApi;
import net.modcrafters.minecraft.barnyardromance.breeding.api.Parent;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;

final class BreedingApiImpl implements BreedingApi {
    @Override
    public Sex newbornSex(double roll, double femaleRatio) {
        return SexRoll.roll(roll, femaleRatio);
    }

    @Override
    public double wildScale(Sex sex, SizeSettings size, double roll) {
        return ScaleRoll.rollWild(sex, size, roll);
    }

    @Override
    public double scaleAt(Sex sex, SizeSettings size, double percent) {
        return ScaleRoll.fromPercent(sex, size, percent);
    }

    @Override
    public AnimalTraits newbornTraits(Sex sex, SizeSettings size, Parent mother, Parent father, double nutrition, RandomSource random,
                                      long bornTick, Lineage lineage) {
        return Offspring.traits(sex, size, mother, father, nutrition, random, bornTick, lineage);
    }
}
