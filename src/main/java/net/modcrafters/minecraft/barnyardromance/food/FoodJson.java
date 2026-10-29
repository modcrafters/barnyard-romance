package net.modcrafters.minecraft.barnyardromance.food;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// Reads diet and food files. A broken file throws IllegalArgumentException; numbers out of range clamp.
public final class FoodJson {
    private static final double MAX_FACTOR = 4.0;
    private static final double MAX_HEAL = 1024.0;
    private static final double MIN_SPEED = 0.1;
    private static final double MAX_SPEED = 10.0;
    private static final long MAX_SPEED_TICKS = 480_000L;

    private FoodJson() {
    }

    public static Diet diet(String json) {
        return dietFrom(parse(json));
    }

    public static Diet dietFrom(JsonElement element) {
        JsonObject object = object(element, "diet");
        List<DietEntry> eats = new ArrayList<>();
        for (JsonElement entry : list(object, "eats")) {
            JsonObject eat = object(entry, "eats entry");
            eats.add(new DietEntry(items(eat), Math.clamp(number(eat, "factor", 1.0), 0.0, MAX_FACTOR)));
        }
        return new Diet(ids(object, "species"), eats, matches(list(object, "refuses")));
    }

    public static FoodFile foodFile(String json) {
        return foodFileFrom(parse(json));
    }

    public static FoodFile foodFileFrom(JsonElement element) {
        JsonObject object = object(element, "food file");
        List<String> species = ids(object, "species");
        List<String> diets = ids(object, "diets");
        if (species.isEmpty() && diets.isEmpty()) {
            throw new IllegalArgumentException("A food file needs species or diets");
        }
        boolean superfood = object.has("superfood") && bool(object.get("superfood"), "superfood");
        FoodEffects effects = object.has("effects") ? effects(object(object.get("effects"), "effects")) : FoodEffects.NONE;
        return new FoodFile(species, diets, items(object), superfood, effects);
    }

    private static FoodEffects effects(JsonObject object) {
        Optional<FoodEffects.PregnancySpeed> speed = Optional.empty();
        if (object.has("pregnancySpeed")) {
            JsonObject speedObject = object(object.get("pregnancySpeed"), "pregnancySpeed");
            speed = Optional.of(new FoodEffects.PregnancySpeed(
                    Math.clamp(number(speedObject, "factor", 2.0), MIN_SPEED, MAX_SPEED),
                    Math.clamp(Math.round(number(speedObject, "ticks", 24_000.0)), 1L, MAX_SPEED_TICKS)
            ));
        }
        return new FoodEffects(
                optional(object, "familiarity").map(value -> Math.clamp(value, 0.0, 1.0)),
                optional(object, "heal").map(value -> Math.clamp(value, 0.0, MAX_HEAL)),
                speed,
                Math.clamp(number(object, "pregnancyProgress", 0.0), -1.0, 1.0),
                Math.clamp(number(object, "babyImprove", 0.0), 0.0, 1.0),
                object.has("startsMilk") && bool(object.get("startsMilk"), "startsMilk")
        );
    }

    private static List<ItemMatch> items(JsonObject object) {
        if (!object.has("items")) {
            throw new IllegalArgumentException("Missing items");
        }
        List<ItemMatch> items = matches(oneOrMany(object.get("items")));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Empty items");
        }
        return items;
    }

    private static List<ItemMatch> matches(List<JsonElement> elements) {
        return mapStrings(elements, ItemMatch::parse);
    }

    private static List<String> ids(JsonObject object, String name) {
        return mapStrings(list(object, name), ItemMatch::id);
    }

    private static <T> List<T> mapStrings(List<JsonElement> elements, Function<String, T> parse) {
        List<T> values = new ArrayList<>();
        for (JsonElement element : elements) {
            if (!(element instanceof JsonPrimitive primitive) || !primitive.isString()) {
                throw new IllegalArgumentException("Expected a string, got " + element);
            }
            values.add(parse.apply(primitive.getAsString()));
        }
        return values;
    }

    private static List<JsonElement> list(JsonObject object, String name) {
        return object.has(name) ? oneOrMany(object.get(name)) : List.of();
    }

    private static List<JsonElement> oneOrMany(JsonElement element) {
        if (element instanceof JsonArray array) {
            return array.asList();
        }
        return List.of(element);
    }

    private static Optional<Double> optional(JsonObject object, String name) {
        return object.has(name) ? Optional.of(number(object, name, 0.0)) : Optional.empty();
    }

    private static double number(JsonObject object, String name, double fallback) {
        JsonElement element = object.get(name);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }
        if (!(element instanceof JsonPrimitive primitive) || !primitive.isNumber() || !Double.isFinite(primitive.getAsDouble())) {
            throw new IllegalArgumentException(name + " must be a number");
        }
        return primitive.getAsDouble();
    }

    private static boolean bool(JsonElement element, String name) {
        if (!(element instanceof JsonPrimitive primitive) || !primitive.isBoolean()) {
            throw new IllegalArgumentException(name + " must be true or false");
        }
        return primitive.getAsBoolean();
    }

    private static JsonObject object(JsonElement element, String what) {
        if (!(element instanceof JsonObject object)) {
            throw new IllegalArgumentException("A " + what + " must be a JSON object");
        }
        return object;
    }

    private static JsonElement parse(String json) {
        try {
            return JsonParser.parseString(json);
        } catch (JsonParseException exception) {
            throw new IllegalArgumentException("Invalid JSON", exception);
        }
    }
}
