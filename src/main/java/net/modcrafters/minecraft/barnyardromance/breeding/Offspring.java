package net.modcrafters.minecraft.barnyardromance.breeding;

import net.modcrafters.minecraft.barnyardromance.core.age.Growth;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.Origin;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;
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
        double familiarity = Familiarity.inherited(nutrition);
        // Born into your care: farm-raised as soon as it has any familiarity at all.
        return AnimalTraits.of(sex, scale)
                .withGrowthTicks(growth)
                .with(FamiliaritySlices.CARE, FamiliarityState.NONE.fed(familiarity, -1L).withOrigin(familiarity > 0.0 ? Origin.FARM : Origin.UNKNOWN))
                .withWeight(weight)
                .withBornTick(bornTick)
                .withLineage(lineage);
    }

    private static double inherit(RandomSource random, double mother, double father, double nutrition) {
        return Inheritance.child(mother, father, random.nextDouble(), random.nextDouble(), nutrition);
    }
}
