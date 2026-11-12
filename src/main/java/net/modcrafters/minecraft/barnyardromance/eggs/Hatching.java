package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.event.Events;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import net.modcrafters.minecraft.barnyardromance.breeding.api.BreedingApi;
import net.modcrafters.minecraft.barnyardromance.breeding.api.Parent;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;
//? if >=1.21.2
import net.minecraft.world.entity.EntitySpawnReason;
//? if >=1.21.5 {
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.chicken.Chicken;
//?}

import java.util.Optional;

// Chicks from eggs. One hatched in a nesting box inherits from both parents like a calf (Offspring) and joins its
// mother's herd books; one from a wild egg is rolled like an animal met in the wild; a thrown one is made by ThrownEggs. Either way it gets a baby's age, its mother's look and
// a place to stand. A box chick that hatched while nobody was near is born at hatchedAt and has grown since.
public final class Hatching {
    private Hatching() {
    }

    public static Optional<Animal> hatch(ServerLevel level, EggData data, Vec3 position, long hatchedAt) {
        Animal chick;
        try {
            Optional<Animal> created = create(level, data.species());
            if (created.isEmpty()) {
                Constants.LOG.warn("Couldn't hatch a {} egg: there is no such animal", data.species());
                return Optional.empty();
            }
            chick = created.get();
            long bornTick = Math.min(hatchedAt, level.getGameTime());
            BarnyardConfig config = BarnyardConfig.current();
            BreedingApi breeding = FeaturesRepository.get(BreedingApi.class);
            AnimalTraits traits = breeding.newbornTraits(
                    breeding.newbornSex(chick.getRandom().nextDouble(), config.femaleRatio()),
                    config.sizeFor(data.species()),
                    new Parent(data.motherScale(), data.motherGrowth(), data.motherWeight()),
                    new Parent(data.fatherScale(), data.fatherGrowth(), data.fatherWeight()),
                    Math.clamp(data.motherFamiliarity(), 0.0, 1.0),
                    chick.getRandom(),
                    bornTick,
                    new Lineage(data.mother(), data.father())
            );
            Services.DATA.set(chick, traits);
            place(chick, data.variant(), (int) Math.max(0L, traits.growthTicks() - (level.getGameTime() - bornTick)), position);
            if (!level.tryAddFreshEntityWithPassengers(chick)) {
                Constants.LOG.warn("Couldn't hatch a {} egg: the level refused the chick", data.species());
                return Optional.empty();
            }
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to hatch a {} egg", data.species(), exception);
            return Optional.empty();
        }
        Events.post(new AnimalEvent.Hatched(level, chick, new Lineage(data.mother(), data.father()), AnimalEvent.Hatch.NEST));
        return Optional.of(chick);
    }

    // A wild egg's chick is like one met in the wild: a wild size, no parents, so it joins no herd book.
    public static Optional<Animal> hatchWild(ServerLevel level, WildEgg egg, Vec3 position, long hatchedAt) {
        try {
            Optional<Animal> created = create(level, egg.species());
            if (created.isEmpty()) {
                Constants.LOG.warn("Couldn't hatch a wild {} egg: there is no such animal", egg.species());
                return Optional.empty();
            }
            Animal chick = created.get();
            long bornTick = Math.min(hatchedAt, level.getGameTime());
            BarnyardConfig config = BarnyardConfig.current();
            BreedingApi breeding = FeaturesRepository.get(BreedingApi.class);
            Sex sex = breeding.newbornSex(chick.getRandom().nextDouble(), config.femaleRatio());
            double scale = breeding.wildScale(sex, config.sizeFor(egg.species()), chick.getRandom().nextDouble());
            AnimalTraits traits = AnimalTraits.of(sex, scale).withBornTick(bornTick);
            Services.DATA.set(chick, traits);
            place(chick, egg.variant(), (int) Math.max(0L, traits.growthTicks() - (level.getGameTime() - bornTick)), position);
            if (!level.tryAddFreshEntityWithPassengers(chick)) {
                Constants.LOG.warn("Couldn't hatch a wild {} egg: the level refused the chick", egg.species());
                return Optional.empty();
            }
            Events.post(new AnimalEvent.Hatched(level, chick, null, AnimalEvent.Hatch.WILD));
            return Optional.of(chick);
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to hatch a wild {} egg", egg.species(), exception);
            return Optional.empty();
        }
    }

    static Optional<Animal> create(ServerLevel level, String species) {
        Identifier id = Identifier.tryParse(species);
        if (id == null) {
            return Optional.empty();
        }
        //? if >=1.21.2 {
        Optional<Entity> entity = BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(type -> type.create(level, EntitySpawnReason.TRIGGERED));
        //?} else
        //Optional<Entity> entity = BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(type -> type.create(level));
        return entity.filter(Animal.class::isInstance).map(Animal.class::cast);
    }

    // growthTicks is how long the chick has left to grow; 0 hatches it grown.
    static void place(Animal chick, String variantId, int growthTicks, Vec3 position) {
        chick.setBaby(true);
        chick.setAge(-growthTicks);
        //? if >=1.21.5 {
        Identifier variant = Identifier.tryParse(variantId);
        if (chick instanceof Chicken chicken && variant != null && !variantId.isEmpty()) {
            chicken.registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT)
                    .get(ResourceKey.create(Registries.CHICKEN_VARIANT, variant))
                    .ifPresent(chicken::setVariant);
        }
        //?}
        float yaw = chick.getRandom().nextFloat() * 360.0F;
        //? if >=1.21.5 {
        chick.snapTo(position.x, position.y, position.z, yaw, 0.0F);
        //?} else
        //chick.moveTo(position.x, position.y, position.z, yaw, 0.0F);
    }
}
