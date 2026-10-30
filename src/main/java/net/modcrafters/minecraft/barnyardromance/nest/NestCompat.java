package net.modcrafters.minecraft.barnyardromance.nest;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.Optional;

// Saving a hen whole while she sits in a box, and bringing her back, UUID and all. versions/1.21.1 has the same
// class for 1.21.1's save calls.
final class NestCompat {
    private NestCompat() {
    }

    static CompoundTag save(Entity entity, ServerLevel level) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Constants.LOG)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
            entity.saveWithoutId(output);
            CompoundTag tag = output.buildResult();
            tag.putString("id", EntityType.getKey(entity.getType()).toString());
            return tag;
        }
    }

    static Optional<Entity> load(ServerLevel level, CompoundTag tag) {
        Identifier id = Identifier.tryParse(tag.getStringOr("id", ""));
        Optional<EntityType<?>> type = id == null ? Optional.empty() : BuiltInRegistries.ENTITY_TYPE.getOptional(id);
        if (type.isEmpty()) {
            return Optional.empty();
        }
        Entity entity = type.get().create(level, EntitySpawnReason.LOAD);
        if (entity == null) {
            return Optional.empty();
        }
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Constants.LOG)) {
            entity.load(TagValueInput.create(reporter, level.registryAccess(), tag));
        }
        return Optional.of(entity);
    }
}
