package net.modcrafters.minecraft.barnyardromance.breeding.api;

import net.minecraft.util.RandomSource;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;

// What other features ask of breeding: a newborn's sex, size and traits.
public interface BreedingApi {
    // A newborn's sex for a roll from 0 to 1: female below femaleRatio.
    Sex newbornSex(double roll, double femaleRatio);

    // The size of an animal met in the wild, which is never in the top half of its sex's range.
    double wildScale(Sex sex, SizeSettings size, double roll);

    // The size at a point (0 to 1) in its sex's range.
    double scaleAt(Sex sex, SizeSettings size, double percent);

    // A newborn's traits from its parents: a calf from a pregnancy and a chick from a fertilized egg alike. nutrition
    // is how well the mother knew her keeper, plus any superfood bonus.
    AnimalTraits newbornTraits(Sex sex, SizeSettings size, Parent mother, Parent father, double nutrition, RandomSource random,
                               long bornTick, Lineage lineage);
}
