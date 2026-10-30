package net.modcrafters.minecraft.barnyardromance.config;

import java.util.Map;

// Built-in pregnancy and litter ranges for vanilla mammals. A user's species entry beats these.
final class SpeciesPresets {
    record Preset(Range pregnancyTicks, Range litter) {
    }

    private static final Preset CATTLE = preset(60_000, 72_000, 1, 1);
    private static final Preset EQUINE = preset(72_000, 96_000, 1, 1);
    private static final Preset SMALL_HERD = preset(36_000, 48_000, 1, 2);
    private static final Preset HOG = preset(30_000, 36_000, 2, 4);
    private static final Preset PREDATOR = preset(24_000, 30_000, 1, 3);
    private static final Preset BEAR = preset(48_000, 60_000, 1, 2);

    private static final Map<String, Preset> PRESETS = Map.ofEntries(
            Map.entry("minecraft:cow", CATTLE),
            Map.entry("minecraft:mooshroom", CATTLE),
            Map.entry("minecraft:horse", EQUINE),
            Map.entry("minecraft:donkey", EQUINE),
            Map.entry("minecraft:llama", CATTLE),
            Map.entry("minecraft:trader_llama", CATTLE),
            Map.entry("minecraft:camel", EQUINE),
            Map.entry("minecraft:sheep", SMALL_HERD),
            Map.entry("minecraft:goat", SMALL_HERD),
            Map.entry("minecraft:pig", HOG),
            Map.entry("minecraft:rabbit", preset(12_000, 18_000, 2, 5)),
            Map.entry("minecraft:wolf", PREDATOR),
            Map.entry("minecraft:fox", PREDATOR),
            Map.entry("minecraft:cat", PREDATOR),
            Map.entry("minecraft:ocelot", PREDATOR),
            Map.entry("minecraft:panda", BEAR),
            Map.entry("minecraft:polar_bear", BEAR),
            Map.entry("minecraft:hoglin", preset(30_000, 36_000, 2, 3))
    );

    private SpeciesPresets() {
    }

    static Preset get(String speciesId) {
        return PRESETS.get(speciesId);
    }

    // Birds that lay eggs instead of giving birth.
    static EggSettings eggs(String speciesId) {
        return "minecraft:chicken".equals(speciesId) ? EggSettings.CHICKEN : EggSettings.NONE;
    }

    private static Preset preset(long pregnancyMin, long pregnancyMax, long litterMin, long litterMax) {
        return new Preset(new Range(pregnancyMin, pregnancyMax), new Range(litterMin, litterMax));
    }
}
