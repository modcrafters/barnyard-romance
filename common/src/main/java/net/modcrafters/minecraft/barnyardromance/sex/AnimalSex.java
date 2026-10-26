package net.modcrafters.minecraft.barnyardromance.sex;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.milk.MilkActions;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public final class AnimalSex {
    private AnimalSex() {
    }

    public static void onJoin(Animal animal) {
        if (isClient(animal)) {
            return;
        }
        rememberAndApply(animal, true);
        MilkActions.onJoin(animal);
    }

    public static void onAgeBoundary(Animal animal) {
        if (isClient(animal)) {
            return;
        }
        rememberAndApply(animal, false);
    }

    private static void rememberAndApply(Animal animal, boolean createIfMissing) {
        BarnyardConfig config = BarnyardConfig.current();
        String speciesId = speciesId(animal);
        Optional<AnimalTraits> existing = Services.DATA.get(animal);
        Optional<AnimalTraits> resolved = createIfMissing
                ? TraitAssignment.ensure(existing, config.speciesEnabled(speciesId), () -> roll(animal, config, speciesId))
                : existing.filter(traits -> config.speciesEnabled(speciesId));
        if (resolved.isEmpty()) {
            return;
        }
        if (createIfMissing && existing.isEmpty()) {
            Services.DATA.set(animal, resolved.get());
        }
        TraitAssignment.scaleToApply(resolved.get(), animal.isBaby())
                .ifPresent(scale -> applyScale(animal, scale));
    }

    private static AnimalTraits roll(Animal animal, BarnyardConfig config, String speciesId) {
        Sex sex = SexRoll.roll(animal.getRandom().nextDouble(), config.femaleRatio());
        double scale = ScaleRoll.rollWild(sex, config.sizeFor(speciesId), animal.getRandom().nextDouble());
        // A baby met for the first time was just born (vanilla breeding, spawn eggs); a wild adult's age is unknown.
        long bornTick = animal.isBaby() ? animal.level().getGameTime() : -1L;
        return new AnimalTraits(sex, scale).withBornTick(bornTick);
    }

    public static String speciesId(Animal animal) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        return id == null ? "unknown" : id.toString();
    }

    public static double currentScale(Animal animal) {
        AttributeInstance attribute = animal.getAttribute(Attributes.SCALE);
        return attribute == null ? 1.0 : attribute.getBaseValue();
    }

    private static void applyScale(Animal animal, double scale) {
        AttributeInstance attribute = animal.getAttribute(Attributes.SCALE);
        if (attribute != null) {
            attribute.setBaseValue(scale);
        }
    }

    private static boolean isClient(Animal animal) {
        return animal.level() != null && animal.level().isClientSide();
    }
}
