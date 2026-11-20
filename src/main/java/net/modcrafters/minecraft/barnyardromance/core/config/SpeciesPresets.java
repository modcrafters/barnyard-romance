package net.modcrafters.minecraft.barnyardromance.core.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

// Built-in pregnancy and litter ranges for vanilla mammals and Naturalist's livestock. A user's species entry beats
// these.
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
            Map.entry("minecraft:hoglin", preset(30_000, 36_000, 2, 3)),
            Map.entry("naturalist:deer", SMALL_HERD),
            Map.entry("naturalist:boar", HOG)
    );

    private SpeciesPresets() {
    }

    static Preset get(String speciesId) {
        return PRESETS.get(speciesId);
    }

    // Birds that lay eggs instead of giving birth. A Naturalist turkey has no egg of its own, so it lays vanilla's.
    static EggSettings eggs(String speciesId) {
        return switch (speciesId) {
            case "minecraft:chicken", "naturalist:turkey" -> EggSettings.CHICKEN;
            case "naturalist:duck" -> EggSettings.CHICKEN.withPlainEgg("naturalist:duck_egg");
            default -> EggSettings.NONE;
        };
    }

    // Livestock males fight their rivals; pets, hunters and everything else are left out unless a species entry says.
    private static final Set<String> FIGHTERS = Set.of(
            "minecraft:cow", "minecraft:mooshroom", "minecraft:sheep", "minecraft:goat", "minecraft:pig", "minecraft:rabbit",
            "minecraft:chicken", "naturalist:deer", "naturalist:boar", "naturalist:duck", "naturalist:turkey", "naturalist:ostrich");

    static boolean fights(String speciesId) {
        return FIGHTERS.contains(speciesId);
    }

    // Animals that stay wild under their mod's own feeding, so familiarity tames them.
    private static final Set<String> TAMES = Set.of(
            "naturalist:duck", "naturalist:ostrich", "untitledduckmod:duck", "untitledduckmod:goose");

    static boolean tames(String speciesId) {
        return TAMES.contains(speciesId);
    }

    private static final String BUCKET = "minecraft:bucket";
    private static final String WOODEN_BUCKET = "meadow:wooden_bucket";
    private static final List<MilkEntry> COW_MILK = List.of(
            new MilkEntry(BUCKET, "minecraft:milk_bucket", null),
            new MilkEntry(WOODEN_BUCKET, "meadow:wooden_milk_bucket", null));
    private static final List<MilkEntry> GOAT_MILK = List.of(
            new MilkEntry(BUCKET, "barnyard_romance:goat_milk_bucket", null),
            new MilkEntry(WOODEN_BUCKET, "meadow:wooden_goat_milk_bucket", null));
    private static final List<MilkEntry> SHEEP_MILK = List.of(
            new MilkEntry(BUCKET, "barnyard_romance:sheep_milk_bucket", null),
            new MilkEntry(WOODEN_BUCKET, "meadow:wooden_sheep_milk_bucket", null));

    // Built-in milking: which items milk the species and what they become. A species entry's own list replaces it.
    static List<MilkEntry> milk(String speciesId) {
        return switch (speciesId) {
            case "minecraft:cow", "minecraft:mooshroom" -> COW_MILK;
            case "minecraft:goat" -> GOAT_MILK;
            case "minecraft:sheep" -> SHEEP_MILK;
            default -> List.of();
        };
    }

    // Every item a built-in entry milks with.
    static List<String> milkingItems() {
        return List.of(BUCKET, WOODEN_BUCKET);
    }

    private static Preset preset(long pregnancyMin, long pregnancyMax, long litterMin, long litterMax) {
        return new Preset(new Range(pregnancyMin, pregnancyMax), new Range(litterMin, litterMax));
    }
}
