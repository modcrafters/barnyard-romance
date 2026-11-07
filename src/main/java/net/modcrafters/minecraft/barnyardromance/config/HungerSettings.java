package net.modcrafters.minecraft.barnyardromance.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

// Whether a hungry pet takes damage, how low it can be starved, how much it loses a day, and how many missed days it
// pays for when the world wakes up after time has passed.
public record HungerSettings(boolean hurts, double minHealth, double dailyDamage, CatchUp catchUp) {
    public enum CatchUp { EVERY_DAY, ONE_DAY, NONE }

    public static final HungerSettings DEFAULTS = new HungerSettings(true, 1.0, 1.0, CatchUp.EVERY_DAY);

    // A bad or missing value keeps its default; this never throws.
    static HungerSettings from(JsonObject object) {
        return new HungerSettings(
                object.get("hurts") instanceof JsonPrimitive primitive && primitive.isBoolean() ? primitive.getAsBoolean() : DEFAULTS.hurts,
                Math.max(0.0, number(object, "minHealth", DEFAULTS.minHealth)),
                Math.max(0.0, number(object, "dailyDamage", DEFAULTS.dailyDamage)),
                catchUp(object.get("catchUp"))
        );
    }

    JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("hurts", hurts);
        object.addProperty("minHealth", minHealth);
        object.addProperty("dailyDamage", dailyDamage);
        object.addProperty("catchUp", catchUp.name());
        return object;
    }

    private static CatchUp catchUp(JsonElement element) {
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            for (CatchUp value : CatchUp.values()) {
                if (value.name().equals(element.getAsString())) {
                    return value;
                }
            }
        }
        return DEFAULTS.catchUp;
    }

    private static double number(JsonObject object, String name, double fallback) {
        JsonElement element = object.get(name);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return fallback;
        }
        double value = element.getAsDouble();
        return Double.isFinite(value) ? value : fallback;
    }
}
