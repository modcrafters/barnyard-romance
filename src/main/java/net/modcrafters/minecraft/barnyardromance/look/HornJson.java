package net.modcrafters.minecraft.barnyardromance.look;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// Reads horn shape files. Anything wrong throws IllegalArgumentException with a message the reload listener logs.
public final class HornJson {
    private HornJson() {
    }

    public static HornFile parse(String json) {
        try {
            return from(JsonParser.parseString(json));
        } catch (JsonSyntaxException exception) {
            throw new IllegalArgumentException("invalid JSON: " + exception.getMessage(), exception);
        }
    }

    public static HornFile from(JsonElement element) {
        JsonObject object = object(element, "horn file");
        List<String> entities = strings(object, "entities");
        if (entities.isEmpty()) {
            throw new IllegalArgumentException("entities must name at least one entity");
        }
        int[] textureSize = textureSize(object);
        return new HornFile(
                entities,
                object.has("attach") ? path(string(object.get("attach"), "attach")) : List.of("head"),
                object.has("offset") ? xyz(object.get("offset"), "offset") : Xyz.ZERO,
                string(required(object, "texture"), "texture"),
                textureSize[0],
                textureSize[1],
                object.has("female_chance") ? Math.clamp(number(object.get("female_chance"), "female_chance"), 0.0, 1.0) : 0.0,
                hide(object),
                parts(object, "male"),
                parts(object, "female")
        );
    }

    // The same part on the other side of the head: X flips, and so do the turns about Y and Z.
    static HornPart mirror(HornPart part) {
        return new HornPart(
                new Xyz(flip(part.pivot().x()), part.pivot().y(), part.pivot().z()),
                new Xyz(part.rotation().x(), flip(part.rotation().y()), flip(part.rotation().z())),
                part.side().mirrored(),
                part.cubes().stream().map(cube -> new HornCube(
                        new Xyz(flip(cube.from().x() + cube.size().x()), cube.from().y(), cube.from().z()),
                        cube.size(),
                        cube.u(),
                        cube.v(),
                        cube.inflate(),
                        !cube.mirror()
                )).toList(),
                part.children().stream().map(HornJson::mirror).toList()
        );
    }

    // Adding 0 turns -0 into 0, so a mirrored 0 still equals 0.
    private static float flip(float value) {
        return -value + 0.0f;
    }

    private static int[] textureSize(JsonObject object) {
        JsonArray size = array(required(object, "texture_size"), "texture_size");
        if (size.size() != 2) {
            throw new IllegalArgumentException("texture_size must be [width, height]");
        }
        int width = (int) number(size.get(0), "texture_size");
        int height = (int) number(size.get(1), "texture_size");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("texture_size must be positive");
        }
        return new int[]{width, height};
    }

    private static List<HideRule> hide(JsonObject object) {
        List<HideRule> rules = new ArrayList<>();
        if (!object.has("hide")) {
            return rules;
        }
        for (JsonElement entry : array(object.get("hide"), "hide")) {
            if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
                rules.add(new HideRule(path(entry.getAsString()), null, null));
            } else if (entry.isJsonObject()) {
                JsonObject rule = entry.getAsJsonObject();
                rules.add(new HideRule(
                        path(string(required(rule, "part"), "part")),
                        xyz(required(rule, "size"), "size"),
                        rule.has("from") ? xyz(rule.get("from"), "from") : null
                ));
            } else {
                throw new IllegalArgumentException("hide entries must be a part path or {part, size, from}");
            }
        }
        return rules;
    }

    private static List<HornPart> parts(JsonObject object, String name) {
        List<HornPart> parts = new ArrayList<>();
        if (!object.has(name)) {
            return parts;
        }
        for (JsonElement entry : array(object.get(name), name)) {
            JsonObject part = object(entry, name + " part");
            HornPart parsed = part(part);
            parts.add(parsed);
            if (part.has("mirror") && bool(part.get("mirror"), "mirror")) {
                parts.add(mirror(parsed));
            }
        }
        return parts;
    }

    private static HornPart part(JsonObject part) {
        List<HornCube> cubes = new ArrayList<>();
        if (part.has("cubes")) {
            for (JsonElement entry : array(part.get("cubes"), "cubes")) {
                cubes.add(cube(object(entry, "cube")));
            }
        }
        return new HornPart(
                xyz(required(part, "pivot"), "pivot"),
                part.has("rotation") ? xyz(part.get("rotation"), "rotation") : Xyz.ZERO,
                part.has("side") ? side(string(part.get("side"), "side")) : HornSide.ANY,
                cubes,
                parts(part, "children")
        );
    }

    private static HornCube cube(JsonObject cube) {
        Xyz size = xyz(required(cube, "size"), "size");
        if (size.x() < 0.0f || size.y() < 0.0f || size.z() < 0.0f) {
            throw new IllegalArgumentException("cube size must not be negative");
        }
        JsonArray uv = array(required(cube, "uv"), "uv");
        if (uv.size() != 2) {
            throw new IllegalArgumentException("uv must be [u, v]");
        }
        return new HornCube(
                xyz(required(cube, "from"), "from"),
                size,
                (int) number(uv.get(0), "uv"),
                (int) number(uv.get(1), "uv"),
                cube.has("inflate") ? (float) number(cube.get("inflate"), "inflate") : 0.0f,
                cube.has("mirror") && bool(cube.get("mirror"), "mirror")
        );
    }

    private static HornSide side(String side) {
        return switch (side.toLowerCase(Locale.ROOT)) {
            case "any" -> HornSide.ANY;
            case "left" -> HornSide.LEFT;
            case "right" -> HornSide.RIGHT;
            default -> throw new IllegalArgumentException("side must be left, right or any, not " + side);
        };
    }

    private static List<String> path(String path) {
        List<String> parts = Arrays.stream(path.split("/")).filter(part -> !part.isEmpty()).toList();
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("a part path must not be empty");
        }
        return parts;
    }

    private static Xyz xyz(JsonElement element, String name) {
        JsonArray array = array(element, name);
        if (array.size() != 3) {
            throw new IllegalArgumentException(name + " must be [x, y, z]");
        }
        return new Xyz((float) number(array.get(0), name), (float) number(array.get(1), name), (float) number(array.get(2), name));
    }

    private static List<String> strings(JsonObject object, String name) {
        JsonElement element = required(object, name);
        if (element.isJsonPrimitive()) {
            return List.of(string(element, name));
        }
        List<String> strings = new ArrayList<>();
        for (JsonElement entry : array(element, name)) {
            strings.add(string(entry, name));
        }
        return strings;
    }

    private static JsonElement required(JsonObject object, String name) {
        if (!object.has(name) || object.get(name).isJsonNull()) {
            throw new IllegalArgumentException("missing " + name);
        }
        return object.get(name);
    }

    private static JsonObject object(JsonElement element, String name) {
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException(name + " must be an object");
        }
        return element.getAsJsonObject();
    }

    private static JsonArray array(JsonElement element, String name) {
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException(name + " must be a list");
        }
        return element.getAsJsonArray();
    }

    private static String string(JsonElement element, String name) {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(name + " must be a string");
        }
        return element.getAsString();
    }

    private static double number(JsonElement element, String name) {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(name + " must be a number");
        }
        return element.getAsDouble();
    }

    private static boolean bool(JsonElement element, String name) {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException(name + " must be true or false");
        }
        return element.getAsBoolean();
    }
}
