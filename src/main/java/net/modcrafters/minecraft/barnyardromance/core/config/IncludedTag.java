package net.modcrafters.minecraft.barnyardromance.core.config;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// The barnyard_romance:included entity tag: a datapack or another mod adds its animals to have the mod track them,
// the same as an enabled species entry in the config (see BarnyardConfig.speciesEnabled).
public final class IncludedTag {
    public static final TagKey<EntityType<?>> KEY = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "included"));
    // Asked several times a tick per animal, so the id lookup is cached; tag membership is read fresh each time.
    private static final Map<String, Optional<EntityType<?>>> TYPES = new ConcurrentHashMap<>();

    private IncludedTag() {
    }

    public static void install() {
        BarnyardConfig.setIncludedByTag(IncludedTag::contains);
    }

    static boolean contains(String speciesId) {
        return TYPES.computeIfAbsent(speciesId, IncludedTag::type).map(type -> type.builtInRegistryHolder().is(KEY)).orElse(false);
    }

    private static Optional<EntityType<?>> type(String speciesId) {
        Identifier id = Identifier.tryParse(speciesId);
        return id == null ? Optional.empty() : BuiltInRegistries.ENTITY_TYPE.getOptional(id);
    }
}
