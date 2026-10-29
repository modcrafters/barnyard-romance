package net.modcrafters.minecraft.barnyardromance.sex;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.milk.MilkActions;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.Optional;

public final class AnimalSex {
    // Fixed IDs, so applying again replaces a wolf's modifiers instead of adding more.
    private static final Identifier WOLF_ATTACK = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "wolf_size_attack");
    private static final Identifier WOLF_HEALTH = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "wolf_size_health");

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
        if (animal instanceof Wolf) {
            applyWolfStrength(animal, resolved.get(), config.sizeFor(speciesId));
        }
    }

    // A grown wolf bites and lasts by its size (see WolfStrength); a pup is a vanilla pup until it grows up.
    private static void applyWolfStrength(Animal animal, AnimalTraits traits, SizeSettings size) {
        boolean baby = animal.isBaby();
        multiply(animal, Attributes.ATTACK_DAMAGE, WOLF_ATTACK, baby ? 1.0 : WolfStrength.attack(traits.scale(), size));
        float maxBefore = animal.getMaxHealth();
        float share = animal.getHealth() / maxBefore;
        multiply(animal, Attributes.MAX_HEALTH, WOLF_HEALTH, baby ? 1.0 : WolfStrength.health(traits.scale(), size));
        // A new maximum keeps the same share of health: a wolf at full health stays full.
        if (animal.getMaxHealth() != maxBefore) {
            animal.setHealth(share * animal.getMaxHealth());
        }
    }

    // Saved with the animal, so a big wolf's health is not cut down to vanilla's maximum while it loads.
    private static void multiply(Animal animal, Holder<Attribute> attribute, Identifier id, double multiplier) {
        AttributeInstance instance = animal.getAttribute(attribute);
        if (instance != null) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(id, multiplier - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
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
