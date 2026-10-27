package net.modcrafters.minecraft.barnyardromance.config;

import net.modcrafters.minecraft.barnyardromance.Constants;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// What this player sees. The server's settings are in BarnyardConfig; these only change how animals are drawn.
public record ClientConfig(boolean belly, boolean horns) {
    public static final ClientConfig DEFAULTS = new ClientConfig(true, true);

    private static ClientConfig current = DEFAULTS;

    public static ClientConfig current() {
        return current;
    }

    // For the client game test, which switches a feature off and back on.
    public static void set(ClientConfig config) {
        current = config;
    }

    public static void load(Path path) {
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                Files.writeString(path, DEFAULTS.toJson());
                current = DEFAULTS;
                return;
            }
            current = parse(Files.readString(path));
        } catch (IOException | IllegalArgumentException exception) {
            Constants.LOG.error("Failed to load {}, using defaults", path, exception);
            current = DEFAULTS;
        }
    }

    public static ClientConfig parse(String json) {
        JsonElement element;
        try {
            element = JsonParser.parseString(json);
        } catch (JsonSyntaxException exception) {
            throw new IllegalArgumentException("Invalid barnyard client config", exception);
        }
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Barnyard client config must be a JSON object");
        }
        JsonObject object = element.getAsJsonObject();
        return new ClientConfig(bool(object, "belly"), bool(object, "horns"));
    }

    public String toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("belly", belly);
        object.addProperty("horns", horns);
        return new GsonBuilder().setPrettyPrinting().create().toJson(object);
    }

    private static boolean bool(JsonObject object, String name) {
        if (!object.has(name) || object.get(name).isJsonNull()) {
            return true;
        }
        if (!object.get(name).isJsonPrimitive() || !object.get(name).getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException(name + " must be a boolean");
        }
        return object.get(name).getAsBoolean();
    }
}
