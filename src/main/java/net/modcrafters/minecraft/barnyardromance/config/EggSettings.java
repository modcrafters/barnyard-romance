package net.modcrafters.minecraft.barnyardromance.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jspecify.annotations.Nullable;

// How a species lays eggs instead of giving birth. plainEgg is the item a hen lays; null means vanilla's egg for the
// hen's variant. Counts are eggs a day, ranges are blocks, and hatchTicks is how long a fertilized egg sits in a box.
public record EggSettings(
        boolean enabled,
        @Nullable String plainEgg,
        Range hatchTicks,
        int smallPerDay,
        int largePerDay,
        int happyBonus,
        int boxRange,
        int roosterRange,
        double thrownHatchChance
) {
    public static final EggSettings NONE = new EggSettings(false, null, new Range(24_000L, 24_000L), 0, 0, 0, 0, 0, 0.0);
    public static final EggSettings CHICKEN = new EggSettings(true, null, new Range(20_000L, 28_000L), 1, 2, 1, 16, 16, 0.125);
    private static final long MAX_HATCH_TICKS = 480_000L;
    private static final int MAX_PER_DAY = 16;
    private static final int MAX_RANGE = 64;

    EggSettings disabled() {
        return new EggSettings(false, plainEgg, hatchTicks, smallPerDay, largePerDay, happyBonus, boxRange, roosterRange, thrownHatchChance);
    }

    // A bad or missing value keeps the fallback's; this never throws.
    static EggSettings from(JsonObject object, EggSettings fallback) {
        Range hatch = Range.of(
                whole(object, "hatchTicksMin", fallback.hatchTicks.min()),
                whole(object, "hatchTicksMax", fallback.hatchTicks.max())
        ).clamp(1L, MAX_HATCH_TICKS);
        return new EggSettings(
                object.has("enabled") && isBoolean(object.get("enabled")) ? object.get("enabled").getAsBoolean() : fallback.enabled,
                object.has("plainEgg") && isString(object.get("plainEgg")) ? object.get("plainEgg").getAsString() : fallback.plainEgg,
                hatch,
                (int) Math.clamp(whole(object, "smallPerDay", fallback.smallPerDay), 0, MAX_PER_DAY),
                (int) Math.clamp(whole(object, "largePerDay", fallback.largePerDay), 0, MAX_PER_DAY),
                (int) Math.clamp(whole(object, "happyBonus", fallback.happyBonus), 0, MAX_PER_DAY),
                (int) Math.clamp(whole(object, "boxRange", fallback.boxRange), 0, MAX_RANGE),
                (int) Math.clamp(whole(object, "roosterRange", fallback.roosterRange), 0, MAX_RANGE),
                Math.clamp(number(object, "thrownHatchChance", fallback.thrownHatchChance), 0.0, 1.0)
        );
    }

    private static long whole(JsonObject object, String name, long fallback) {
        JsonElement element = object.get(name);
        return isNumber(element) ? Math.round(element.getAsDouble()) : fallback;
    }

    private static double number(JsonObject object, String name, double fallback) {
        JsonElement element = object.get(name);
        return isNumber(element) ? element.getAsDouble() : fallback;
    }

    private static boolean isNumber(@Nullable JsonElement element) {
        return element instanceof JsonPrimitive primitive && primitive.isNumber();
    }

    private static boolean isBoolean(JsonElement element) {
        return element instanceof JsonPrimitive primitive && primitive.isBoolean();
    }

    private static boolean isString(JsonElement element) {
        return element instanceof JsonPrimitive primitive && primitive.isString();
    }
}
