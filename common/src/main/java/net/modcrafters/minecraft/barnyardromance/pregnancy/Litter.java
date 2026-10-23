package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Litter {
    private Litter() {
    }

    public static List<CompoundTag> conceive(Animal mother, Animal father, ServerLevel level, int size) {
        List<CompoundTag> litter = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            try {
                AgeableMob baby = mother.getBreedOffspring(level, father);
                if (baby == null) {
                    continue;
                }
                baby.setBaby(true);
                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(baby.problemPath(), Constants.LOG)) {
                    TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
                    if (baby.save(output)) {
                        CompoundTag tag = output.buildResult();
                        tag.remove(Entity.TAG_UUID);
                        litter.add(tag);
                    }
                }
            } catch (RuntimeException exception) {
                Constants.LOG.warn("Failed to conceive a {} offspring, it will take after its mother", AnimalSex.speciesId(mother), exception);
            }
        }
        return litter;
    }

    public static Optional<AgeableMob> deliver(Animal mother, ServerLevel level, List<CompoundTag> litter, int index) {
        if (index < litter.size()) {
            Optional<AgeableMob> stored = load(level, litter.get(index));
            if (stored.isPresent()) {
                return stored;
            }
            Constants.LOG.warn("Could not load a stored {} offspring, it will take after its mother", AnimalSex.speciesId(mother));
        }
        return Optional.ofNullable(mother.getBreedOffspring(level, mother));
    }

    private static Optional<AgeableMob> load(ServerLevel level, CompoundTag tag) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Constants.LOG)) {
            return EntityType.create(
                            TagValueInput.create(reporter, level.registryAccess(), tag),
                            level,
                            new EntitySpawnRequest(EntitySpawnReason.BREEDING, false)
                    )
                    .filter(AgeableMob.class::isInstance)
                    .map(AgeableMob.class::cast);
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to load a stored offspring", exception);
            return Optional.empty();
        }
    }
}
