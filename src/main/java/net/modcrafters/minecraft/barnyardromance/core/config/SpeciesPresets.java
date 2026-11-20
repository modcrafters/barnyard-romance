package net.modcrafters.minecraft.barnyardromance.core.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            Map.entry("friendsandfoes:moobloom", CATTLE),
            Map.entry("meadow:water_buffalo", CATTLE),
            Map.entry("meadow:wooly_cow", CATTLE),
            Map.entry("environmental:yak", CATTLE),
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
            case "autumnity:turkey" -> EggSettings.CHICKEN.withPlainEgg("autumnity:turkey_egg");
            case "environmental:duck" -> EggSettings.CHICKEN.withPlainEgg("environmental:duck_egg");
            case "naturalist:duck" -> EggSettings.CHICKEN.withPlainEgg("naturalist:duck_egg");
            case "untitledduckmod:duck" -> EggSettings.CHICKEN.withPlainEgg("untitledduckmod:duck_egg");
            case "untitledduckmod:goose" -> EggSettings.CHICKEN.withPlainEgg("untitledduckmod:goose_egg");
            default -> EggSettings.NONE;
        };
    }

    // Livestock males fight their rivals; pets, hunters and everything else are left out unless a species entry says.
    private static final Set<String> FIGHTERS = Set.of(
            "minecraft:cow", "minecraft:mooshroom", "minecraft:sheep", "minecraft:goat", "minecraft:pig", "minecraft:rabbit",
            "minecraft:chicken", "naturalist:deer", "naturalist:boar", "naturalist:duck", "naturalist:turkey", "naturalist:ostrich",
            "untitledduckmod:duck", "untitledduckmod:goose", "friendsandfoes:moobloom", "meadow:water_buffalo", "meadow:wooly_cow",
            "autumnity:turkey", "environmental:duck", "environmental:yak");

    static boolean fights(String speciesId) {
        return FIGHTERS.contains(speciesId);
    }

    // Animals that stay wild under their mod's own feeding, so familiarity tames them.
    private static final Set<String> TAMES = Set.of(
            "naturalist:duck", "naturalist:ostrich", "untitledduckmod:duck", "untitledduckmod:goose");

    static boolean tames(String speciesId) {
        return TAMES.contains(speciesId);
    }

    // The item tag of food the species' own mod tames with. Familiarity tames them instead, so the mod's food is turned
    // down (FamiliarityActions.blocksVanillaTaming).
    static Optional<String> tamingFoodTag(String speciesId) {
        return switch (speciesId) {
            case "untitledduckmod:duck" -> Optional.of("untitledduckmod:duck_taming_food");
            case "untitledduckmod:goose" -> Optional.of("untitledduckmod:goose_taming_food");
            default -> Optional.empty();
        };
    }

    private static final String BUCKET = "minecraft:bucket";
    private static final String WOODEN_BUCKET = "meadow:wooden_bucket";
    private static final List<MilkEntry> COW_MILK = List.of(
            new MilkEntry(BUCKET, "minecraft:milk_bucket", null),
            new MilkEntry(WOODEN_BUCKET, "meadow:wooden_milk_bucket", null));
    private static final List<MilkEntry> BUFFALO_MILK = List.of(
            new MilkEntry(BUCKET, "minecraft:milk_bucket", null),
            new MilkEntry(WOODEN_BUCKET, "meadow:wooden_buffalo_milk_bucket", null));
    private static final List<MilkEntry> GOAT_MILK = List.of(
            new MilkEntry(BUCKET, "barnyard_romance:goat_milk_bucket", null),
            new MilkEntry(WOODEN_BUCKET, "meadow:wooden_goat_milk_bucket", null));
    private static final List<MilkEntry> SHEEP_MILK = List.of(
            new MilkEntry(BUCKET, "barnyard_romance:sheep_milk_bucket", null),
            new MilkEntry(WOODEN_BUCKET, "meadow:wooden_sheep_milk_bucket", null));

    // Environmental's yak is milked with an iron bucket only.
    private static final List<MilkEntry> YAK_MILK = List.of(new MilkEntry(BUCKET, "minecraft:milk_bucket", null));

    // Built-in milking: which items milk the species and what they become. A species entry's own list replaces it.
    private static final Map<String, List<MilkEntry>> MILK = Map.of(
            "minecraft:cow", COW_MILK,
            "minecraft:mooshroom", COW_MILK,
            "friendsandfoes:moobloom", COW_MILK,
            "meadow:water_buffalo", BUFFALO_MILK,
            "meadow:wooly_cow", COW_MILK,
            "environmental:yak", YAK_MILK,
            "minecraft:goat", GOAT_MILK,
            "minecraft:sheep", SHEEP_MILK);

    static List<MilkEntry> milk(String speciesId) {
        return MILK.getOrDefault(speciesId, List.of());
    }

    // Bowls give stew from the mooshroom (mushroom stew) and the moobloom (suspicious stew); a species entry's "stew"
    // can add or take away.
    static boolean stew(String speciesId) {
        return "minecraft:mooshroom".equals(speciesId) || "friendsandfoes:moobloom".equals(speciesId);
    }

    // Every item a built-in entry milks with, taken from the rows above.
    static Set<String> milkingItems() {
        return MILK.values().stream().flatMap(List::stream).map(MilkEntry::bucket).collect(java.util.stream.Collectors.toSet());
    }

    private static Preset preset(long pregnancyMin, long pregnancyMax, long litterMin, long litterMax) {
        return new Preset(new Range(pregnancyMin, pregnancyMax), new Range(litterMin, litterMax));
    }
}
