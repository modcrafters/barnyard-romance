package net.modcrafters.minecraft.barnyardromance.config;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BarnyardConfig {
    private static final SizeSettings DEFAULT_SIZE = new SizeSettings(0.8, 1.2, 0.1);
    private static final long DEFAULT_PREGNANCY_TICKS = 24_000L;
    private static final int DEFAULT_LITTER_SIZE = 1;
    private static final long MAX_PREGNANCY_TICKS = 480_000L;
    private static final int MAX_LITTER_SIZE = 8;

    private static BarnyardConfig current = defaults();

    private final boolean enabled;
    private final double femaleRatio;
    private final SizeSettings defaults;
    private final boolean pregnancyEnabled;
    private final long pregnancyTicks;
    private final int litterSize;
    private final Map<String, SpeciesSettings> species;

    private BarnyardConfig(
            boolean enabled,
            double femaleRatio,
            SizeSettings defaults,
            boolean pregnancyEnabled,
            long pregnancyTicks,
            int litterSize,
            Map<String, SpeciesSettings> species
    ) {
        this.enabled = enabled;
        this.femaleRatio = femaleRatio;
        this.defaults = defaults;
        this.pregnancyEnabled = pregnancyEnabled;
        this.pregnancyTicks = pregnancyTicks;
        this.litterSize = litterSize;
        this.species = species;
    }

    public static BarnyardConfig defaults() {
        return new BarnyardConfig(true, 0.5, DEFAULT_SIZE, true, DEFAULT_PREGNANCY_TICKS, DEFAULT_LITTER_SIZE, Map.of());
    }

    public static BarnyardConfig current() {
        return current;
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
        double femaleRatio = clamp(number(object, "femaleRatio", 0.5), 0.0, 1.0);
        SizeSettings size = new SizeSettings(
                number(object, "femaleScale", DEFAULT_SIZE.femaleScale()),
                number(object, "maleScale", DEFAULT_SIZE.maleScale()),
                Math.max(0.0, number(object, "scaleVariation", DEFAULT_SIZE.variation()))
        );
        boolean pregnancyEnabled = bool(object, "pregnancyEnabled", true);
        long pregnancyTicks = clampLong(Math.round(number(object, "pregnancyTicks", DEFAULT_PREGNANCY_TICKS)), 1L, MAX_PREGNANCY_TICKS);
        int litterSize = (int) clampLong(Math.round(number(object, "litterSize", DEFAULT_LITTER_SIZE)), 1L, MAX_LITTER_SIZE);
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
                femaleRatio,
                size,
                pregnancyEnabled,
                pregnancyTicks,
                litterSize,
                Collections.unmodifiableMap(species)
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
        object.addProperty("femaleRatio", femaleRatio);
        object.addProperty("femaleScale", defaults.femaleScale());
        object.addProperty("maleScale", defaults.maleScale());
        object.addProperty("scaleVariation", defaults.variation());
        object.addProperty("pregnancyEnabled", pregnancyEnabled);
        object.addProperty("pregnancyTicks", pregnancyTicks);
        object.addProperty("litterSize", litterSize);
        if (!species.isEmpty()) {
            JsonObject speciesObject = new JsonObject();
            for (Map.Entry<String, SpeciesSettings> entry : species.entrySet()) {
                speciesObject.add(entry.getKey(), entry.getValue().toJson());
            }
            object.add("species", speciesObject);
        }
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        jsonWriter.setIndent("  ");
        write(jsonWriter, object);
        jsonWriter.close();
        return writer.toString();
    }

    public double femaleRatio() {
        return femaleRatio;
    }

    public SizeSettings defaultSize() {
        return defaults;
    }

    public boolean speciesEnabled(String speciesId) {
        if (!enabled) {
            return false;
        }
        SpeciesSettings settings = species.get(speciesId);
        return settings == null || settings.enabled() == null || settings.enabled();
    }

    public boolean pregnancyEnabled() {
        return enabled && pregnancyEnabled;
    }

    public long pregnancyTicks(String speciesId) {
        SpeciesSettings settings = species.get(speciesId);
        long value = settings == null || settings.pregnancyTicks() == null ? pregnancyTicks : settings.pregnancyTicks();
        return clampLong(value, 1L, MAX_PREGNANCY_TICKS);
    }

    public int litterSize(String speciesId) {
        SpeciesSettings settings = species.get(speciesId);
        int value = settings == null || settings.litterSize() == null ? litterSize : settings.litterSize();
        return (int) clampLong(value, 1L, MAX_LITTER_SIZE);
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

    private static double clamp(double value, double min, double max) {
        return Math.clamp(value, min, max);
    }

    private static long clampLong(long value, long min, long max) {
        return Math.clamp(value, min, max);
    }

    private record SpeciesSettings(
            Boolean enabled,
            Double femaleScale,
            Double maleScale,
            Double variation,
            Long pregnancyTicks,
            Integer litterSize
    ) {
        static SpeciesSettings from(JsonObject object) {
            return new SpeciesSettings(
                    object.has("enabled") ? bool(object, "enabled", true) : null,
                    object.has("femaleScale") ? number(object, "femaleScale", 0.0) : null,
                    object.has("maleScale") ? number(object, "maleScale", 0.0) : null,
                    object.has("scaleVariation") ? number(object, "scaleVariation", 0.0) : null,
                    object.has("pregnancyTicks") ? Math.round(number(object, "pregnancyTicks", 0.0)) : null,
                    object.has("litterSize") ? (int) Math.round(number(object, "litterSize", 0.0)) : null
            );
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
            if (pregnancyTicks != null) {
                object.addProperty("pregnancyTicks", pregnancyTicks);
            }
            if (litterSize != null) {
                object.addProperty("litterSize", litterSize);
            }
            return object;
        }
    }
}
