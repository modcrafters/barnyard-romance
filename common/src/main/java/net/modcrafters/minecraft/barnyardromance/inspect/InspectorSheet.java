package net.modcrafters.minecraft.barnyardromance.inspect;

import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;

import java.util.Locale;

public record InspectorSheet(
        String title,
        String subtitle,
        boolean known,
        boolean female,
        String sizeLetter,
        String life,
        String scaleText,
        double familiarity,
        String familiarityText,
        double feed,
        String feedText,
        double growth,
        String growthText,
        float health,
        float maxHealth,
        boolean pregnant,
        double pregnancy,
        String pregnancyText,
        String fatherText
) {
    public static InspectorSheet unknown(String name, String typeName, boolean named, boolean baby, float health, float maxHealth) {
        return new InspectorSheet(
                named ? name : typeName,
                named ? typeName : "",
                false,
                false,
                "",
                baby ? "Baby" : "Adult",
                "",
                0.0,
                "",
                0.0,
                "",
                0.0,
                "",
                health,
                maxHealth,
                false,
                0.0,
                "",
                ""
        );
    }

    public static String scaleText(double scale) {
        return "scale " + String.format(Locale.ROOT, "%.2f", scale);
    }

    public static String feedText(long lastFedTick, long now) {
        long wait = Familiarity.ticksUntilFeed(lastFedTick, now);
        if (wait <= 0L) {
            return "Ready";
        }
        return Growth.clock((int) Math.min(wait, Integer.MAX_VALUE));
    }

    public static String father(String sizeLetter, String speciesId) {
        return "Father " + sizeLetter + " " + mate(speciesId);
    }

    public static String mate(String speciesId) {
        if (speciesId == null || speciesId.isBlank()) {
            return "mate";
        }
        String path = speciesId;
        int colon = path.indexOf(':');
        if (colon >= 0 && colon + 1 < path.length()) {
            path = path.substring(colon + 1);
        }
        return switch (path) {
            case "cow", "mooshroom" -> "bull";
            case "pig" -> "boar";
            case "chicken" -> "rooster";
            case "sheep" -> "ram";
            case "rabbit", "goat" -> "buck";
            case "horse" -> "stallion";
            case "donkey" -> "jack";
            case "cat" -> "tom";
            default -> path.replace('_', ' ');
        };
    }

    public String healthText() {
        return hp(health) + "/" + hp(maxHealth);
    }

    public boolean showPregnancy() {
        return known && (pregnant || ("Adult".equals(life) && female));
    }

    private static String hp(float value) {
        if (value == (int) value) {
            return Integer.toString((int) value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
