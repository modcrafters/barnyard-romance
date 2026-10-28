package net.modcrafters.minecraft.barnyardromance.sex;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

// An animal's parents, set at birth and never changed. Null means unknown: wild, spawned, or from an older save.
public record Lineage(@Nullable UUID mother, @Nullable UUID father) {
    public static final Lineage UNKNOWN = new Lineage(null, null);

    public boolean knows(UUID id) {
        return id.equals(mother) || id.equals(father);
    }
}
