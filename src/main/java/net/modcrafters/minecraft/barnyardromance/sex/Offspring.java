package net.modcrafters.minecraft.barnyardromance.sex;

import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.minecraft.util.RandomSource;

// A newborn's traits from its parents, by the one inheritance rule (Inheritance.child): a calf from a pregnancy and a
// chick from a fertilized egg both come from here. nutrition is how well the mother knew her keeper, plus any
// superfood bonus; it shapes every trait and the young one's own familiarity.
public final class Offspring {
    private Offspring() {
    }

    public record Parent(double scale, int growthTicks, double weight) {
    }

    public static AnimalTraits traits(
            Sex sex,
            SizeSettings size,
            Parent mother,
            Parent father,
            double nutrition,
            RandomSource random,
            long bornTick,
            Lineage lineage
    ) {
        double scalePercent = inherit(random, ScaleRoll.percent(Sex.FEMALE, mother.scale(), size), ScaleRoll.percent(Sex.MALE, father.scale(), size), nutrition);
        double scale = ScaleRoll.fromPercent(sex, size, scalePercent);
        int growth = Growth.fromPercent(inherit(random, Growth.percent(mother.growthTicks()), Growth.percent(father.growthTicks()), nutrition));
        double weight = inherit(random, mother.weight(), father.weight(), nutrition);
        return new AnimalTraits(sex, scale, Familiarity.inherited(nutrition), -1L, growth)
                .withWeight(weight)
                .withBornTick(bornTick)
                .withLineage(lineage);
    }

    private static double inherit(RandomSource random, double mother, double father, double nutrition) {
        return Inheritance.child(mother, father, random.nextDouble(), random.nextDouble(), nutrition);
    }
}
