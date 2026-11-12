package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.event.Events;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.age.Growth;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import net.modcrafters.minecraft.barnyardromance.breeding.ScaleRoll;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.breeding.SexRoll;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClass;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

// Thrown eggs. A plain egg never hatches while its species lays by the mod's rules; a fertilized one sometimes
// does, but it was out in the weather, so the chick is small, light and has no known parents.
public final class ThrownEggs {
    private static final double TAINTED_WEIGHT = 0.25;
    // The middle of the S and M size bands.
    private static final double SMALL = 0.125;
    private static final double MEDIUM = 0.375;

    private ThrownEggs() {
    }

    // Vanilla's chicks from thrown eggs are only left alone when chickens are excluded.
    public static boolean blocksVanillaChicks(ItemStack thrown) {
        return thrown.is(EggsItems.FERTILIZED_EGG) || BarnyardConfig.current().eggsFor("minecraft:chicken").enabled();
    }

    public static void land(ServerLevel level, Vec3 position, ItemStack thrown, RandomSource random) {
        EggData data = thrown.get(EggsComponents.EGG_DATA);
        WildEgg wild = thrown.get(EggsComponents.WILD_EGG);
        String species = data != null ? data.species() : wild != null ? wild.species() : null;
        if (species == null) {
            return;
        }
        try {
            if (random.nextDouble() < BarnyardConfig.current().eggsFor(species).thrownHatchChance()) {
                if (data != null) {
                    hatchTainted(level, position, data, random);
                } else {
                    hatchTainted(level, position, wild, random);
                }
            }
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to hatch a thrown {} egg", species, exception);
        }
    }

    public static Optional<Animal> hatchTainted(ServerLevel level, Vec3 position, EggData data, RandomSource random) {
        SizeSettings size = BarnyardConfig.current().sizeFor(data.species());
        Optional<Animal> chick = hatchTainted(level, position, data.species(), data.variant(),
                SizeClass.of(Sex.FEMALE, data.motherScale(), size), SizeClass.of(Sex.MALE, data.fatherScale(), size), random);
        chick.ifPresent(hatched -> Events.post(
                new AnimalEvent.Hatched(level, hatched, new Lineage(data.mother(), data.father()), AnimalEvent.Hatch.THROWN)));
        return chick;
    }

    // A wild egg's parents are unknown, so its chick is sized as if both were average.
    public static Optional<Animal> hatchTainted(ServerLevel level, Vec3 position, WildEgg egg, RandomSource random) {
        Optional<Animal> chick = hatchTainted(level, position, egg.species(), egg.variant(), SizeClass.M, SizeClass.M, random);
        chick.ifPresent(hatched -> Events.post(new AnimalEvent.Hatched(level, hatched, null, AnimalEvent.Hatch.THROWN)));
        return chick;
    }

    private static Optional<Animal> hatchTainted(ServerLevel level, Vec3 position, String species, String variant,
                                                 SizeClass mother, SizeClass father, RandomSource random) {
        Optional<Animal> created = Hatching.create(level, species);
        if (created.isEmpty()) {
            return Optional.empty();
        }
        Animal chick = created.get();
        BarnyardConfig config = BarnyardConfig.current();
        SizeSettings size = config.sizeFor(species);
        Sex sex = SexRoll.roll(random.nextDouble(), config.femaleRatio());
        SizeClass chickSize = EggRules.thrownChickSize(mother, father, random.nextDouble());
        double scale = ScaleRoll.fromPercent(sex, size, chickSize == SizeClass.M ? MEDIUM : SMALL);
        int growth = Growth.wild();
        Services.DATA.set(chick, AnimalTraits.of(sex, scale)
                .withGrowthTicks(growth)
                .withWeight(TAINTED_WEIGHT)
                .withBornTick(level.getGameTime()));
        Hatching.place(chick, variant, growth, position);
        level.addFreshEntityWithPassengers(chick);
        return Optional.of(chick);
    }
}
