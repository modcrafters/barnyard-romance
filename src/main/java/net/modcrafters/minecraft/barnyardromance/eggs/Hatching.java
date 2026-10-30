package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Lineage;
import net.modcrafters.minecraft.barnyardromance.sex.Offspring;
import net.modcrafters.minecraft.barnyardromance.sex.SexRoll;
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

import java.util.List;
import java.util.Optional;

// Chicks from eggs. One hatched in a nesting box inherits from both parents like a calf (Offspring) and joins its
// mother's herd books; a thrown one is made by ThrownEggs. Either way it gets a baby's age, its mother's look and
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
            AnimalTraits traits = Offspring.traits(
                    SexRoll.roll(chick.getRandom().nextDouble(), config.femaleRatio()),
                    config.sizeFor(data.species()),
                    new Offspring.Parent(data.motherScale(), data.motherGrowth(), data.motherWeight()),
                    new Offspring.Parent(data.fatherScale(), data.fatherGrowth(), data.fatherWeight()),
                    Math.clamp(data.motherFamiliarity(), 0.0, 1.0),
                    chick.getRandom(),
                    bornTick,
                    new Lineage(data.mother(), data.father())
            );
            Services.DATA.set(chick, traits);
            place(chick, data, (int) Math.max(0L, traits.growthTicks() - (level.getGameTime() - bornTick)), position);
            if (!level.tryAddFreshEntityWithPassengers(chick)) {
                Constants.LOG.warn("Couldn't hatch a {} egg: the level refused the chick", data.species());
                return Optional.empty();
            }
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to hatch a {} egg", data.species(), exception);
            return Optional.empty();
        }
        // The chick is out by now, so a herd book problem mustn't count as a failed hatch.
        try {
            HerdBooks.born(level, data.mother(), List.of(chick));
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to add a hatched {} to herd books", data.species(), exception);
        }
        return Optional.of(chick);
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
    static void place(Animal chick, EggData data, int growthTicks, Vec3 position) {
        chick.setBaby(true);
        chick.setAge(-growthTicks);
        //? if >=1.21.5 {
        Identifier variant = Identifier.tryParse(data.variant());
        if (chick instanceof Chicken chicken && variant != null && !data.variant().isEmpty()) {
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
