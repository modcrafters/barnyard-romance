package net.modcrafters.minecraft.barnyardromance.core.config;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class BarnyardConfig {
    private static final SizeSettings DEFAULT_SIZE = new SizeSettings(0.87, 1.13, 0.1);
    private static final Range DEFAULT_PREGNANCY_TICKS = new Range(24_000L, 24_000L);
    private static final Range DEFAULT_LITTER = new Range(1L, 1L);
    private static final long MAX_PREGNANCY_TICKS = 480_000L;
    private static final long MAX_LITTER_SIZE = 8L;
    private static final long MAX_AGE_TICKS = 24_000_000L;
    // A belly that starts at the very end would never show.
    private static final double MAX_BELLY_START = 0.99;

    private static BarnyardConfig current = defaults();
    // Whether a species is in the barnyard_romance:excluded entity tag. Tags are Minecraft's, so the mod installs this
    // at start-up; until then (and in unit tests) nothing is excluded by tag.
    private static Predicate<String> excludedByTag = speciesId -> false;
    // The same for the barnyard_romance:included tag.
    private static Predicate<String> includedByTag = speciesId -> false;

    private final boolean enabled;
    private final boolean herdSticks;
    private final boolean goatLongJumps;
    private final boolean colouredSheep;
    private final boolean sheepDyeing;
    private final boolean milkConversion;
    private final double femaleRatio;
    private final SizeSettings defaults;
    private final boolean pregnancyEnabled;
    private final Range pregnancyTicks;
    private final Range litter;
    private final AgeSettings age;
    private final BellySettings belly;
    private final Map<String, SpeciesSettings> species;
    private final List<String> exclude;
    private final HungerSettings hunger;
    private final Tuning tuning;
    // eggsFor runs several times per chicken per tick, so each species' eggs block is read once.
    private final Map<String, EggSettings> eggs = new ConcurrentHashMap<>();
    // Every item that milks something, for the bucket checks on every right-click; found on first use.
    private volatile Set<String> milkingItems;

    private BarnyardConfig(
            boolean enabled,
            boolean herdSticks,
            boolean goatLongJumps,
            boolean colouredSheep,
            boolean sheepDyeing,
            boolean milkConversion,
            double femaleRatio,
            SizeSettings defaults,
            boolean pregnancyEnabled,
            Range pregnancyTicks,
            Range litter,
            AgeSettings age,
            BellySettings belly,
            Map<String, SpeciesSettings> species,
            List<String> exclude,
            HungerSettings hunger,
            Tuning tuning
    ) {
        this.enabled = enabled;
        this.herdSticks = herdSticks;
        this.goatLongJumps = goatLongJumps;
        this.colouredSheep = colouredSheep;
        this.sheepDyeing = sheepDyeing;
        this.milkConversion = milkConversion;
        this.femaleRatio = femaleRatio;
        this.defaults = defaults;
        this.pregnancyEnabled = pregnancyEnabled;
        this.pregnancyTicks = pregnancyTicks;
        this.litter = litter;
        this.age = age;
        this.belly = belly;
        this.species = species;
        this.exclude = exclude;
        this.hunger = hunger;
        this.tuning = tuning;
    }

    public static BarnyardConfig defaults() {
        return new BarnyardConfig(true, herdSticksByDefault(), false, false, false, false, 0.5, DEFAULT_SIZE, true, DEFAULT_PREGNANCY_TICKS, DEFAULT_LITTER, AgeSettings.DEFAULTS, BellySettings.DEFAULTS, Map.of(), List.of(), HungerSettings.DEFAULTS, Tuning.DEFAULTS);
    }

    // The dev client runs set -Dbarnyard_romance.herd_sticks=true; a config file's own herdSticks wins over it.
    public static boolean herdSticksByDefault() {
        return Boolean.getBoolean("barnyard_romance.herd_sticks");
    }

    public static BarnyardConfig current() {
        return current;
    }

    public static void setExcludedByTag(Predicate<String> excluded) {
        excludedByTag = excluded;
    }

    public static void setIncludedByTag(Predicate<String> included) {
        includedByTag = included;
    }

    // For game tests, which swap a config in and restore it within the same tick.
    public static void set(BarnyardConfig config) {
        current = config;
    }

    public static void load(Path path) {
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                Files.writeString(path, defaults().toJson());
                current = defaults();
                return;
            }
            current = parse(Files.readString(path));
        } catch (IOException | IllegalArgumentException exception) {
            Constants.LOG.error("Failed to load {}, using defaults", path, exception);
            current = defaults();
        }
    }

    public static BarnyardConfig parse(String json) {
        JsonElement element;
        try {
            element = JsonParser.parseString(json);
        } catch (JsonSyntaxException exception) {
            throw new IllegalArgumentException("Invalid barnyard config", exception);
        }
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Barnyard config must be a JSON object");
        }
        JsonObject object = element.getAsJsonObject();
        boolean enabled = bool(object, "enabled", true);
        boolean herdSticks = bool(object, "herdSticks", herdSticksByDefault());
        boolean goatLongJumps = bool(object, "goatLongJumps", false);
        boolean colouredSheep = bool(object, "colouredSheep", false);
        boolean sheepDyeing = bool(object, "sheepDyeing", false);
        boolean milkConversion = bool(object, "milkConversion", false);
        double femaleRatio = clamp(number(object, "femaleRatio", 0.5), 0.0, 1.0);
        SizeSettings size = new SizeSettings(
                number(object, "femaleScale", DEFAULT_SIZE.femaleScale()),
                number(object, "maleScale", DEFAULT_SIZE.maleScale()),
                Math.max(0.0, number(object, "scaleVariation", DEFAULT_SIZE.variation()))
        );
        boolean pregnancyEnabled = bool(object, "pregnancyEnabled", true);
        Range pregnancyTicks = range(object, "pregnancyTicks", DEFAULT_PREGNANCY_TICKS).clamp(1L, MAX_PREGNANCY_TICKS);
        Range litter = range(object, "litterSize", "litterMin", "litterMax", DEFAULT_LITTER).clamp(1L, MAX_LITTER_SIZE);
        AgeSettings age = new AgeSettings(
                ageTicks(optionalLong(object, "youngTicks"), AgeSettings.DEFAULTS.youngTicks()),
                ageTicks(optionalLong(object, "oldAfterTicks"), AgeSettings.DEFAULTS.oldAfterTicks()),
                ageTicks(optionalLong(object, "deathAfterTicks"), AgeSettings.DEFAULTS.deathAfterTicks())
        );
        BellySettings belly = new BellySettings(
                bellyMaxWiden(optionalDouble(object, "bellyMaxWiden"), BellySettings.DEFAULTS.maxWiden()),
                bellyStart(optionalDouble(object, "bellyStart"), BellySettings.DEFAULTS.start())
        );
        Map<String, SpeciesSettings> species = new LinkedHashMap<>();
        if (object.has("species") && !object.get("species").isJsonNull()) {
            if (!object.get("species").isJsonObject()) {
                throw new IllegalArgumentException("species must be an object");
            }
            for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject("species").entrySet()) {
                if (!entry.getValue().isJsonObject()) {
                    throw new IllegalArgumentException("species " + entry.getKey() + " must be an object");
                }
                species.put(entry.getKey(), SpeciesSettings.from(entry.getValue().getAsJsonObject()));
            }
        }
        return new BarnyardConfig(
                enabled,
                herdSticks,
                goatLongJumps,
                colouredSheep,
                sheepDyeing,
                milkConversion,
                femaleRatio,
                size,
                pregnancyEnabled,
                pregnancyTicks,
                litter,
                age,
                belly,
                Collections.unmodifiableMap(species),
                exclude(object),
                object.get("hunger") instanceof JsonObject hunger ? HungerSettings.from(hunger) : HungerSettings.DEFAULTS,
                object.get("tuning") instanceof JsonObject tuning ? Tuning.from(tuning) : Tuning.DEFAULTS
        );
    }

    public String toJson() {
        try {
            return writeJson();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write barnyard config", exception);
        }
    }

    private String writeJson() throws IOException {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", enabled);
        object.addProperty("herdSticks", herdSticks);
        object.addProperty("goatLongJumps", goatLongJumps);
        object.addProperty("colouredSheep", colouredSheep);
        object.addProperty("sheepDyeing", sheepDyeing);
        object.addProperty("milkConversion", milkConversion);
        object.addProperty("femaleRatio", femaleRatio);
        object.addProperty("femaleScale", defaults.femaleScale());
        object.addProperty("maleScale", defaults.maleScale());
        object.addProperty("scaleVariation", defaults.variation());
        object.addProperty("pregnancyEnabled", pregnancyEnabled);
        object.addProperty("pregnancyTicksMin", pregnancyTicks.min());
        object.addProperty("pregnancyTicksMax", pregnancyTicks.max());
        object.addProperty("litterMin", litter.min());
        object.addProperty("litterMax", litter.max());
        object.addProperty("youngTicks", age.youngTicks());
        object.addProperty("oldAfterTicks", age.oldAfterTicks());
        object.addProperty("deathAfterTicks", age.deathAfterTicks());
        object.addProperty("bellyMaxWiden", belly.maxWiden());
        object.addProperty("bellyStart", belly.start());
        if (!species.isEmpty()) {
            JsonObject speciesObject = new JsonObject();
            for (Map.Entry<String, SpeciesSettings> entry : species.entrySet()) {
                speciesObject.add(entry.getKey(), entry.getValue().toJson());
            }
            object.add("species", speciesObject);
        }
        JsonArray excludeArray = new JsonArray();
        exclude.forEach(excludeArray::add);
        object.add("exclude", excludeArray);
        object.add("hunger", hunger.toJson());
        object.add("tuning", tuning.toJson());
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        jsonWriter.setIndent("  ");
        write(jsonWriter, object);
        jsonWriter.close();
        return writer.toString();
    }

    // Whether the Herd Sticks and the commands that hand them out exist. Commands and the tab read it when the server
    // starts, so a change needs a restart.
    public boolean herdSticks() {
        return herdSticks;
    }

    // Whether goats the mod tracks keep vanilla's long jump, which carries them over a pen's fence. Off by default.
    public boolean goatLongJumps() {
        return goatLongJumps;
    }

    // Whether tracked sheep keep vanilla's coloured fleeces when they spawn and pass their colour to their lambs. Off by
    // default: they spawn white and have white lambs.
    public boolean colouredSheep() {
        return colouredSheep;
    }

    // Whether dye works on tracked sheep. Off by default.
    public boolean sheepDyeing() {
        return sheepDyeing;
    }

    // Whether the recipe that turns a goat or sheep milk bucket (or any other bucket in c:buckets/milk) into a vanilla
    // milk bucket loads. Off by default; read at startup, as a recipe's load condition.
    public boolean milkConversion() {
        return milkConversion;
    }

    public double femaleRatio() {
        return femaleRatio;
    }

    public HungerSettings hunger() {
        return hunger;
    }

    public Tuning tuning() {
        return tuning;
    }

    public SizeSettings defaultSize() {
        return defaults;
    }

    // In order: the species entry's own "enabled", then the exclude list and the excluded tag, then the included tag, then
    // the built-in list (vanilla farm, working and pet animals, and what features track by default). A species entry's
    // "enabled" wins, so one species of an excluded mod can be switched back on.
    public boolean speciesEnabled(String speciesId) {
        return speciesEnabled(speciesId, true);
    }

    // The same without the two tags, for hooks that must tell "a pack switched this animal off" from "never tracked".
    public boolean speciesEnabledIgnoringTags(String speciesId) {
        return speciesEnabled(speciesId, false);
    }

    private boolean speciesEnabled(String speciesId, boolean useTags) {
        if (!enabled) {
            return false;
        }
        SpeciesSettings settings = species.get(speciesId);
        if (settings != null && settings.enabled() != null) {
            return settings.enabled();
        }
        if (excluded(speciesId) || useTags && excludedByTag.test(speciesId)) {
            return false;
        }
        if (useTags && includedByTag.test(speciesId)) {
            return true;
        }
        return BuiltInSpecies.VANILLA.contains(speciesId) || FeaturesRepository.trackedByDefault(speciesId);
    }

    // Animals that stay wild under their mod's own feeding, so familiarity tames them.
    public boolean tamesThroughFamiliarity(String speciesId) {
        return SpeciesPresets.tames(speciesId) && speciesEnabled(speciesId);
    }

    // The item tag of food this species' mod tames it with, for a species that familiarity tames instead.
    public Optional<String> tamingFoodTag(String speciesId) {
        return tamesThroughFamiliarity(speciesId) ? SpeciesPresets.tamingFoodTag(speciesId) : Optional.empty();
    }

    public List<String> exclude() {
        return exclude;
    }

    private boolean excluded(String speciesId) {
        for (String entry : exclude) {
            if (entry.equals(speciesId) || entry.endsWith(":*") && speciesId.startsWith(entry.substring(0, entry.length() - 1))) {
                return true;
            }
        }
        return false;
    }

    // Each entry is a species id or a whole mod ("somemod:*"); a bad entry is skipped and logged, the rest still apply.
    private static List<String> exclude(JsonObject object) {
        if (!object.has("exclude") || object.get("exclude").isJsonNull()) {
            return List.of();
        }
        if (!object.get("exclude").isJsonArray()) {
            Constants.LOG.warn("Ignoring the barnyard config's exclude: it must be a list");
            return List.of();
        }
        List<String> entries = new ArrayList<>();
        for (JsonElement element : object.getAsJsonArray("exclude")) {
            String entry = element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() ? element.getAsString() : "";
            int colon = entry.indexOf(':');
            if (colon <= 0 || colon == entry.length() - 1) {
                Constants.LOG.warn("Ignoring exclude entry {}: it must be a species id or \"mod:*\"", element);
                continue;
            }
            entries.add(entry);
        }
        return List.copyOf(entries);
    }

    public boolean pregnancyEnabled() {
        return enabled && pregnancyEnabled;
    }

    // Each end is looked up on its own: the user's species entry, then the built-in preset, then the global value.
    public Range pregnancyRange(String speciesId) {
        SpeciesSettings settings = species.get(speciesId);
        SpeciesPresets.Preset preset = SpeciesPresets.get(speciesId);
        Range fallback = preset == null ? pregnancyTicks : preset.pregnancyTicks();
        return Range.of(
                settings == null || settings.pregnancyTicksMin() == null ? fallback.min() : settings.pregnancyTicksMin(),
                settings == null || settings.pregnancyTicksMax() == null ? fallback.max() : settings.pregnancyTicksMax()
        ).clamp(1L, MAX_PREGNANCY_TICKS);
    }

    public Range litterRange(String speciesId) {
        SpeciesSettings settings = species.get(speciesId);
        SpeciesPresets.Preset preset = SpeciesPresets.get(speciesId);
        Range fallback = preset == null ? litter : preset.litter();
        return Range.of(
                settings == null || settings.litterMin() == null ? fallback.min() : settings.litterMin(),
                settings == null || settings.litterMax() == null ? fallback.max() : settings.litterMax()
        ).clamp(1L, MAX_LITTER_SIZE);
    }

    // The user's species entry, then the global value, for each of the three on its own.
    public AgeSettings ageFor(String speciesId) {
        SpeciesSettings settings = species.get(speciesId);
        if (settings == null) {
            return age;
        }
        return new AgeSettings(
                ageTicks(settings.youngTicks(), age.youngTicks()),
                ageTicks(settings.oldAfterTicks(), age.oldAfterTicks()),
                ageTicks(settings.deathAfterTicks(), age.deathAfterTicks())
        );
    }

    // The user's species entry, then the global value, for each of the two on its own.
    public BellySettings bellyFor(String speciesId) {
        SpeciesSettings settings = species.get(speciesId);
        if (settings == null) {
            return belly;
        }
        return new BellySettings(
                bellyMaxWiden(settings.bellyMaxWiden(), belly.maxWiden()),
                bellyStart(settings.bellyStart(), belly.start())
        );
    }

    // The diet the user picked for this species, or null to use the diet that lists it.
    public String dietOverride(String speciesId) {
        SpeciesSettings settings = species.get(speciesId);
        return settings == null ? null : settings.diet();
    }

    // The species entry's eggs block over the built-in preset. Only chickens lay by default; a block on any other
    // species starts from the chicken's numbers but has to switch itself on.
    public EggSettings eggsFor(String speciesId) {
        if (!speciesEnabled(speciesId)) {
            return EggSettings.NONE;
        }
        return eggs.computeIfAbsent(speciesId, id -> {
            EggSettings preset = SpeciesPresets.eggs(id);
            SpeciesSettings settings = species.get(id);
            if (settings == null || settings.eggs() == null) {
                return preset;
            }
            return EggSettings.from(settings.eggs(), preset.enabled() ? preset : EggSettings.CHICKEN.disabled());
        });
    }

    // The entity id shearing turns this species into for good, if it does (a mooshroom shears into a cow).
    public Optional<String> shearsInto(String speciesId) {
        return SpeciesPresets.shearsInto(speciesId);
    }

    // Whether males of this species fight their rivals: the species entry's "fights", else the built-in livestock list.
    public boolean fightsFor(String speciesId) {
        if (!speciesEnabled(speciesId)) {
            return false;
        }
        SpeciesSettings settings = species.get(speciesId);
        if (settings != null && settings.fights() != null) {
            return settings.fights();
        }
        return SpeciesPresets.fights(speciesId);
    }

    // A species entry's milk list replaces the built-in one; with none, the preset's entries. Nothing for a species the
    // mod leaves alone.
    public MilkSettings milkFor(String speciesId) {
        if (!speciesEnabled(speciesId)) {
            return MilkSettings.NONE;
        }
        SpeciesSettings settings = species.get(speciesId);
        if (settings != null && settings.milk() != null) {
            return new MilkSettings(settings.milk(), true);
        }
        return new MilkSettings(SpeciesPresets.milk(speciesId), false);
    }

    // Whether a bowl held out to this species gives mushroom stew: the species entry's "stew", else the built-in
    // mooshroom. False for a species the mod leaves alone.
    public boolean stewFor(String speciesId) {
        if (!speciesEnabled(speciesId)) {
            return false;
        }
        SpeciesSettings settings = species.get(speciesId);
        if (settings != null && settings.stew() != null) {
            return settings.stew();
        }
        return SpeciesPresets.stew(speciesId);
    }

    // Whether the once-a-day shearing (and its familiarity cost) counts for this species: the species entry's
    // "shearing", else on. False for a species the mod leaves alone.
    public boolean shearingFor(String speciesId) {
        if (!speciesEnabled(speciesId)) {
            return false;
        }
        SpeciesSettings settings = species.get(speciesId);
        return settings == null || settings.shearing() == null || settings.shearing();
    }

    // Whether the held item milks some species: milkingItems names its id, or a #tag it's in.
    public boolean isMilkingItem(ItemStack held) {
        if (held.isEmpty()) {
            return false;
        }
        Set<String> items = milkingItems();
        if (items.contains(BuiltInRegistries.ITEM.getKey(held.getItem()).toString())) {
            return true;
        }
        for (String item : items) {
            if (MilkEntry.inTag(item, held)) {
                return true;
            }
        }
        return false;
    }

    // Every item that milks some species, in a preset or in any config list, whichever species is on. An entry that
    // names a tag is here as # and the tag id.
    public Set<String> milkingItems() {
        Set<String> items = milkingItems;
        if (items == null) {
            Set<String> found = new LinkedHashSet<>(SpeciesPresets.milkingItems());
            for (SpeciesSettings settings : species.values()) {
                if (settings.milk() != null) {
                    settings.milk().forEach(entry -> found.add(entry.bucket()));
                }
            }
            items = Collections.unmodifiableSet(found);
            milkingItems = items;
        }
        return items;
    }

    public SizeSettings sizeFor(String speciesId) {
        SpeciesSettings settings = species.get(speciesId);
        if (settings == null) {
            return defaults;
        }
        return new SizeSettings(
                settings.femaleScale() == null ? defaults.femaleScale() : settings.femaleScale(),
                settings.maleScale() == null ? defaults.maleScale() : settings.maleScale(),
                settings.variation() == null ? defaults.variation() : Math.max(0.0, settings.variation())
        );
    }

    private static void write(JsonWriter writer, JsonElement element) throws IOException {
        if (element.isJsonObject()) {
            writer.beginObject();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                writer.name(entry.getKey());
                write(writer, entry.getValue());
            }
            writer.endObject();
            return;
        }
        if (element.isJsonArray()) {
            writer.beginArray();
            for (JsonElement item : element.getAsJsonArray()) {
                write(writer, item);
            }
            writer.endArray();
            return;
        }
        if (element.isJsonPrimitive()) {
            var primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                writer.value(primitive.getAsBoolean());
            } else if (primitive.isNumber()) {
                writer.value(primitive.getAsDouble());
            } else {
                writer.value(primitive.getAsString());
            }
            return;
        }
        throw new IllegalArgumentException("Unsupported config json value");
    }

    private static boolean bool(JsonObject object, String name, boolean fallback) {
        if (!object.has(name) || object.get(name).isJsonNull()) {
            return fallback;
        }
        if (!object.get(name).isJsonPrimitive() || !object.get(name).getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException(name + " must be a boolean");
        }
        return object.get(name).getAsBoolean();
    }

    private static double number(JsonObject object, String name, double fallback) {
        if (!object.has(name) || object.get(name).isJsonNull()) {
            return fallback;
        }
        if (!object.get(name).isJsonPrimitive() || !object.get(name).getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(name + " must be a number");
        }
        return object.get(name).getAsDouble();
    }

    private static Range range(JsonObject object, String legacy, Range fallback) {
        return range(object, legacy, legacy + "Min", legacy + "Max", fallback);
    }

    // An old single value (litterSize, pregnancyTicks) sets both ends; the min/max keys beat it.
    private static Range range(JsonObject object, String legacy, String minName, String maxName, Range fallback) {
        Long single = optionalLong(object, legacy);
        Long min = optionalLong(object, minName);
        Long max = optionalLong(object, maxName);
        return Range.of(
                min != null ? min : single != null ? single : fallback.min(),
                max != null ? max : single != null ? single : fallback.max()
        );
    }

    private static Long optionalLong(JsonObject object, String name) {
        if (!object.has(name) || object.get(name).isJsonNull()) {
            return null;
        }
        return Math.round(number(object, name, 0.0));
    }

    private static Double optionalDouble(JsonObject object, String name) {
        if (!object.has(name) || object.get(name).isJsonNull()) {
            return null;
        }
        return number(object, name, 0.0);
    }

    private static double bellyMaxWiden(Double value, double fallback) {
        return Math.clamp(value == null ? fallback : value, 0.0, 1.0);
    }

    private static double bellyStart(Double value, double fallback) {
        return Math.clamp(value == null ? fallback : value, 0.0, MAX_BELLY_START);
    }

    private static String optionalString(JsonObject object, String name) {
        if (!object.has(name) || object.get(name).isJsonNull()) {
            return null;
        }
        if (!object.get(name).isJsonPrimitive() || !object.get(name).getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(name + " must be a string");
        }
        return object.get(name).getAsString();
    }

    private static long ageTicks(Long value, long fallback) {
        return Math.clamp(value == null ? fallback : value, 0L, MAX_AGE_TICKS);
    }

    private static double clamp(double value, double min, double max) {
        return Math.clamp(value, min, max);
    }

    private record SpeciesSettings(
            Boolean enabled,
            Double femaleScale,
            Double maleScale,
            Double variation,
            Long pregnancyTicksMin,
            Long pregnancyTicksMax,
            Long litterMin,
            Long litterMax,
            Long youngTicks,
            Long oldAfterTicks,
            Long deathAfterTicks,
            String diet,
            Double bellyMaxWiden,
            Double bellyStart,
            JsonObject eggs,
            Boolean fights,
            List<MilkEntry> milk,
            Boolean stew,
            Boolean shearing
    ) {
        static SpeciesSettings from(JsonObject object) {
            Long pregnancyTicks = optionalLong(object, "pregnancyTicks");
            Long litterSize = optionalLong(object, "litterSize");
            Long pregnancyTicksMin = optionalLong(object, "pregnancyTicksMin");
            Long pregnancyTicksMax = optionalLong(object, "pregnancyTicksMax");
            Long litterMin = optionalLong(object, "litterMin");
            Long litterMax = optionalLong(object, "litterMax");
            return new SpeciesSettings(
                    object.has("enabled") ? bool(object, "enabled", true) : null,
                    object.has("femaleScale") ? number(object, "femaleScale", 0.0) : null,
                    object.has("maleScale") ? number(object, "maleScale", 0.0) : null,
                    object.has("scaleVariation") ? number(object, "scaleVariation", 0.0) : null,
                    pregnancyTicksMin != null ? pregnancyTicksMin : pregnancyTicks,
                    pregnancyTicksMax != null ? pregnancyTicksMax : pregnancyTicks,
                    litterMin != null ? litterMin : litterSize,
                    litterMax != null ? litterMax : litterSize,
                    optionalLong(object, "youngTicks"),
                    optionalLong(object, "oldAfterTicks"),
                    optionalLong(object, "deathAfterTicks"),
                    optionalString(object, "diet"),
                    optionalDouble(object, "bellyMaxWiden"),
                    optionalDouble(object, "bellyStart"),
                    object.has("eggs") && object.get("eggs").isJsonObject() ? object.getAsJsonObject("eggs") : null,
                    object.has("fights") ? bool(object, "fights", false) : null,
                    milk(object),
                    object.has("stew") ? bool(object, "stew", false) : null,
                    object.has("shearing") ? bool(object, "shearing", true) : null
            );
        }

        // A bad entry is skipped and logged, like exclude's; the good ones stay. Not a list: the built-in list is used.
        private static List<MilkEntry> milk(JsonObject object) {
            if (!object.has("milk") || object.get("milk").isJsonNull()) {
                return null;
            }
            if (!object.get("milk").isJsonArray()) {
                Constants.LOG.warn("Ignoring a species entry's milk: it must be a list");
                return null;
            }
            List<MilkEntry> entries = new ArrayList<>();
            for (JsonElement element : object.getAsJsonArray("milk")) {
                String bucket = stringIn(element, "bucket");
                String result = stringIn(element, "result");
                if (bucket == null || result == null) {
                    Constants.LOG.warn("Ignoring milk entry {}: it needs a \"bucket\" (an item id or #tag) and a \"result\" (an item id)", element);
                    continue;
                }
                JsonElement sound = element.getAsJsonObject().get("sound");
                if (sound != null && !sound.isJsonNull() && stringIn(element, "sound") == null) {
                    Constants.LOG.warn("Ignoring the sound of milk entry {}: it must be a sound id", element);
                }
                entries.add(new MilkEntry(bucket, result, stringIn(element, "sound")));
            }
            return List.copyOf(entries);
        }

        private static String stringIn(JsonElement element, String name) {
            if (!element.isJsonObject()) {
                return null;
            }
            JsonElement value = element.getAsJsonObject().get(name);
            return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() ? value.getAsString() : null;
        }

        JsonObject toJson() {
            JsonObject object = new JsonObject();
            if (enabled != null) {
                object.addProperty("enabled", enabled);
            }
            if (femaleScale != null) {
                object.addProperty("femaleScale", femaleScale);
            }
            if (maleScale != null) {
                object.addProperty("maleScale", maleScale);
            }
            if (variation != null) {
                object.addProperty("scaleVariation", variation);
            }
            if (pregnancyTicksMin != null) {
                object.addProperty("pregnancyTicksMin", pregnancyTicksMin);
            }
            if (pregnancyTicksMax != null) {
                object.addProperty("pregnancyTicksMax", pregnancyTicksMax);
            }
            if (litterMin != null) {
                object.addProperty("litterMin", litterMin);
            }
            if (litterMax != null) {
                object.addProperty("litterMax", litterMax);
            }
            if (youngTicks != null) {
                object.addProperty("youngTicks", youngTicks);
            }
            if (oldAfterTicks != null) {
                object.addProperty("oldAfterTicks", oldAfterTicks);
            }
            if (deathAfterTicks != null) {
                object.addProperty("deathAfterTicks", deathAfterTicks);
            }
            if (diet != null) {
                object.addProperty("diet", diet);
            }
            if (bellyMaxWiden != null) {
                object.addProperty("bellyMaxWiden", bellyMaxWiden);
            }
            if (bellyStart != null) {
                object.addProperty("bellyStart", bellyStart);
            }
            if (eggs != null) {
                object.add("eggs", eggs);
            }
            if (fights != null) {
                object.addProperty("fights", fights);
            }
            if (milk != null) {
                JsonArray array = new JsonArray();
                for (MilkEntry entry : milk) {
                    JsonObject item = new JsonObject();
                    item.addProperty("bucket", entry.bucket());
                    item.addProperty("result", entry.result());
                    if (entry.sound() != null) {
                        item.addProperty("sound", entry.sound());
                    }
                    array.add(item);
                }
                object.add("milk", array);
            }
            if (stew != null) {
                object.addProperty("stew", stew);
            }
            if (shearing != null) {
                object.addProperty("shearing", shearing);
            }
            return object;
        }
    }
}
