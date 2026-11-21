package net.modcrafters.minecraft.barnyardromance.core.config;

import java.util.Set;

// The vanilla animals the mod tracks by default: farm animals, working animals, dogs and cats. Everything else is
// tracked only when a config entry, the included tag or a feature's trackedByDefault says so.
public final class BuiltInSpecies {
    public static final Set<String> VANILLA = Set.of(
            "minecraft:cow", "minecraft:mooshroom", "minecraft:sheep", "minecraft:goat", "minecraft:pig", "minecraft:chicken",
            "minecraft:rabbit", "minecraft:hoglin", "minecraft:horse", "minecraft:donkey", "minecraft:mule", "minecraft:llama",
            "minecraft:trader_llama", "minecraft:camel", "minecraft:wolf", "minecraft:cat");

    private BuiltInSpecies() {
    }
}
