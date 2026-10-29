package net.modcrafters.minecraft.barnyardromance.config;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// The barnyard_romance:excluded entity tag: a datapack or another mod adds its animals to turn the mod off for them,
// the same as an exclude entry in the config (see BarnyardConfig.speciesEnabled).
public final class ExcludedTag {
    public static final TagKey<EntityType<?>> KEY = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "excluded"));
    // Asked several times a tick per animal, so the id lookup is cached; tag membership is read fresh each time.
    private static final Map<String, Optional<EntityType<?>>> TYPES = new ConcurrentHashMap<>();

    private ExcludedTag() {
    }

    public static void install() {
        BarnyardConfig.setExcludedByTag(ExcludedTag::contains);
    }

    static boolean contains(String speciesId) {
        return TYPES.computeIfAbsent(speciesId, ExcludedTag::type).map(type -> type.builtInRegistryHolder().is(KEY)).orElse(false);
    }

    private static Optional<EntityType<?>> type(String speciesId) {
        Identifier id = Identifier.tryParse(speciesId);
        return id == null ? Optional.empty() : BuiltInRegistries.ENTITY_TYPE.getOptional(id);
    }
}
