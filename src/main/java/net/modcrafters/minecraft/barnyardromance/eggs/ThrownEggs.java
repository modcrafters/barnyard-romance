package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.sex.ScaleRoll;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SexRoll;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
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
        return thrown.is(BarnyardItems.FERTILIZED_EGG) || BarnyardConfig.current().eggsFor("minecraft:chicken").enabled();
    }

    public static void land(ServerLevel level, Vec3 position, ItemStack thrown, RandomSource random) {
        EggData data = thrown.get(BarnyardComponents.EGG_DATA);
        if (data == null) {
            return;
        }
        try {
            if (random.nextDouble() < BarnyardConfig.current().eggsFor(data.species()).thrownHatchChance()) {
                hatchTainted(level, position, data, random);
            }
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to hatch a thrown {} egg", data.species(), exception);
        }
    }

    public static Optional<Animal> hatchTainted(ServerLevel level, Vec3 position, EggData data, RandomSource random) {
        Optional<Animal> created = Hatching.create(level, data.species());
        if (created.isEmpty()) {
            return Optional.empty();
        }
        Animal chick = created.get();
        BarnyardConfig config = BarnyardConfig.current();
        SizeSettings size = config.sizeFor(data.species());
        Sex sex = SexRoll.roll(random.nextDouble(), config.femaleRatio());
        SizeClass chickSize = EggRules.thrownChickSize(
                SizeClass.of(Sex.FEMALE, data.motherScale(), size),
                SizeClass.of(Sex.MALE, data.fatherScale(), size),
                random.nextDouble()
        );
        double scale = ScaleRoll.fromPercent(sex, size, chickSize == SizeClass.M ? MEDIUM : SMALL);
        int growth = Growth.wild();
        Services.DATA.set(chick, new AnimalTraits(sex, scale, 0.0, -1L, growth)
                .withWeight(TAINTED_WEIGHT)
                .withBornTick(level.getGameTime()));
        Hatching.place(chick, data, growth, position);
        level.addFreshEntityWithPassengers(chick);
        return Optional.of(chick);
    }
}
