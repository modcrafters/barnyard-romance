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
        double weight,
        String weightText,
        float health,
        float maxHealth,
        double fertility,
        String fertilityText,
        boolean pregnant,
        double pregnancy,
        String pregnancyText,
        String fatherText,
        boolean breedingReady,
        String breedingText,
        String lastBirthText
) {
    public static final String READY = "Ready";
    public static final String NOT_TAMED = "Not tamed";
    public static final String IN_LOVE = "In love";
    public static final String WORN_OUT = "Worn out";

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
                0.0,
                "",
                health,
                maxHealth,
                0.0,
                "",
                false,
                0.0,
                "",
                "",
                false,
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

    // Love and rest are stored as the game tick they end, so they expire on the client without a new sync.
    public static String breeding(boolean needsTaming, boolean tamed, double familiarity, boolean wornOut, long loveUntil, long cooldownUntil, long now) {
        if (needsTaming && !tamed) {
            return NOT_TAMED;
        }
        if (!Familiarity.ready(familiarity)) {
            return "Needs " + Familiarity.percent(Familiarity.threshold()) + " familiarity";
        }
        if (cooldownUntil > now) {
            return "Resting " + Growth.clock((int) Math.min(cooldownUntil - now, Integer.MAX_VALUE));
        }
        if (wornOut) {
            return WORN_OUT;
        }
        if (loveUntil > now) {
            return IN_LOVE;
        }
        return READY;
    }

    public static boolean readyToBreed(String breedingText) {
        return READY.equals(breedingText) || IN_LOVE.equals(breedingText);
    }

    public static String lastBirth(String offspringName, long lastBirthTick, long now) {
        if (lastBirthTick < 0L || offspringName == null || offspringName.isBlank()) {
            return "";
        }
        return "Last birth: " + offspringName + ", " + ago(now - lastBirthTick);
    }

    public static String ago(long ticks) {
        long seconds = Math.max(0L, ticks) / 20L;
        if (seconds < 60L) {
            return "just now";
        }
        long minutes = seconds / 60L;
        if (minutes < 60L) {
            return minutes + "m ago";
        }
        long hours = minutes / 60L;
        if (hours < 24L) {
            return hours + "h ago";
        }
        return hours / 24L + "d ago";
    }

    public String sexText() {
        return female ? "Female" : "Male";
    }

    public String healthText() {
        return hp(health) + "/" + hp(maxHealth);
    }

    public boolean showBreeding() {
        return known && (pregnant || "Adult".equals(life));
    }

    // Fathers never wear out, so only adult females show fertility.
    public boolean showFertility() {
        return known && female && "Adult".equals(life);
    }

    public String detailText() {
        return pregnant ? fatherText : lastBirthText;
    }

    private static String hp(float value) {
        if (value == (int) value) {
            return Integer.toString((int) value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
