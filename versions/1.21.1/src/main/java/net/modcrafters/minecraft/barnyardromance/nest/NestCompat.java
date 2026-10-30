package net.modcrafters.minecraft.barnyardromance.nest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Optional;

// The 1.21.1 copy of src/.../nest/NestCompat.
final class NestCompat {
    private NestCompat() {
    }

    static CompoundTag save(Entity entity, ServerLevel level) {
        CompoundTag tag = entity.saveWithoutId(new CompoundTag());
        tag.putString("id", EntityType.getKey(entity.getType()).toString());
        return tag;
    }

    static Optional<Entity> load(Level level, CompoundTag tag) {
        Optional<EntityType<?>> type = EntityType.byString(tag.getString("id"));
        if (type.isEmpty()) {
            return Optional.empty();
        }
        Entity entity = type.get().create(level);
        if (entity == null) {
            return Optional.empty();
        }
        entity.load(tag);
        return Optional.of(entity);
    }
}
