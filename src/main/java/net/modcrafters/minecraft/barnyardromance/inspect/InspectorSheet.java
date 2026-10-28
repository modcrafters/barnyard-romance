package net.modcrafters.minecraft.barnyardromance.inspect;

import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.milk.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Title and subtitle are the game's own (already translated) names; everything else is Text for the lang file.
public record InspectorSheet(
        String title,
        String subtitle,
        boolean known,
        boolean female,
        String sizeLetter,
        AgeStage stage,
        Text scaleText,
        double familiarity,
        Text familiarityText,
        double feed,
        Text feedText,
        double growth,
        Text growthText,
        double weight,
        Text weightText,
        float health,
        float maxHealth,
        double fertility,
        Text fertilityText,
        Text milkText,
        boolean pregnant,
        double pregnancy,
        Text pregnancyText,
        Text fatherText,
        boolean breedingReady,
        Text breedingText,
        Text lastBirthText
) {
    public static final Text READY = Text.of("gui.barnyard_romance.inspector.ready");
    public static final Text NOT_TAMED = Text.of("gui.barnyard_romance.inspector.not_tamed");
    public static final Text IN_LOVE = Text.of("gui.barnyard_romance.inspector.in_love");
    public static final Text WORN_OUT = Text.of("gui.barnyard_romance.inspector.worn_out");
    public static final Text TOO_YOUNG = Text.of("gui.barnyard_romance.inspector.too_young");

    public static InspectorSheet unknown(String name, String typeName, boolean named, boolean baby, float health, float maxHealth) {
        return new InspectorSheet(
                named ? name : typeName,
                named ? typeName : "",
                false,
                false,
                "",
                baby ? AgeStage.BABY : AgeStage.ADULT,
                Text.EMPTY,
                0.0,
                Text.EMPTY,
                0.0,
                Text.EMPTY,
                0.0,
                Text.EMPTY,
                0.0,
                Text.EMPTY,
                health,
                maxHealth,
                0.0,
                Text.EMPTY,
                Text.EMPTY,
                false,
                0.0,
                Text.EMPTY,
                Text.EMPTY,
                false,
                Text.EMPTY,
                Text.EMPTY
        );
    }

    public static Text scaleText(double scale) {
        return Text.of("gui.barnyard_romance.inspector.scale", String.format(Locale.ROOT, "%.2f", scale));
    }

    public static Text feedText(long lastFedTick, long now) {
        long wait = Familiarity.ticksUntilFeed(lastFedTick, now);
        if (wait <= 0L) {
            return READY;
        }
        return Growth.clock(wait);
    }

    public static Text father(String sizeLetter, String speciesId) {
        return Text.of("gui.barnyard_romance.inspector.father", sizeLetter, mate(speciesId));
    }

    public static Text mate(String speciesId) {
        if (speciesId == null || speciesId.isBlank()) {
            return Text.of("gui.barnyard_romance.mate.unknown");
        }
        String path = speciesId;
        int colon = path.indexOf(':');
        if (colon >= 0 && colon + 1 < path.length()) {
            path = path.substring(colon + 1);
        }
        return switch (path) {
            case "cow", "mooshroom" -> Text.of("gui.barnyard_romance.mate.bull");
            case "pig" -> Text.of("gui.barnyard_romance.mate.boar");
            case "chicken" -> Text.of("gui.barnyard_romance.mate.rooster");
            case "sheep" -> Text.of("gui.barnyard_romance.mate.ram");
            case "rabbit", "goat" -> Text.of("gui.barnyard_romance.mate.buck");
            case "horse" -> Text.of("gui.barnyard_romance.mate.stallion");
            case "donkey" -> Text.of("gui.barnyard_romance.mate.jack");
            case "cat" -> Text.of("gui.barnyard_romance.mate.tom");
            default -> Text.plain(path.replace('_', ' '));
        };
    }

    // Love and rest are stored as the game tick they end, so they expire on the client without a new sync.
    public static Text breeding(
            boolean needsTaming,
            boolean tamed,
            double familiarity,
            boolean young,
            boolean wornOut,
            long loveUntil,
            long cooldownUntil,
            long now
    ) {
        if (needsTaming && !tamed) {
            return NOT_TAMED;
        }
        if (!Familiarity.ready(familiarity)) {
            return Text.of("gui.barnyard_romance.inspector.needs_familiarity", Familiarity.percent(Familiarity.threshold()));
        }
        if (young) {
            return TOO_YOUNG;
        }
        if (cooldownUntil > now) {
            return Text.of("gui.barnyard_romance.inspector.resting", Growth.clock(cooldownUntil - now));
        }
        if (wornOut) {
            return WORN_OUT;
        }
        if (loveUntil > now) {
            return IN_LOVE;
        }
        return READY;
    }

    public static boolean readyToBreed(Text breedingText) {
        return READY.equals(breedingText) || IN_LOVE.equals(breedingText);
    }

    public static Text lastBirth(String offspringName, long lastBirthTick, long now) {
        if (lastBirthTick < 0L || offspringName == null || offspringName.isBlank()) {
            return Text.EMPTY;
        }
        return Text.of("gui.barnyard_romance.inspector.last_birth", offspringName, ago(now - lastBirthTick));
    }

    // Milk is counted in Minecraft days and hours, the way its three unmilked days are.
    public static Text milkText(MilkStatus status) {
        if (status.nursing()) {
            return Text.of("gui.barnyard_romance.inspector.milk_flowing", status.left(), status.perDay());
        }
        return Text.of("gui.barnyard_romance.inspector.milk_left", gameTime(status.ticksLeft()), status.left(), status.perDay());
    }

    // Minecraft hours and days: under an hour, then hours, then whole days.
    public static Text gameTime(long ticks) {
        long hours = Math.max(0L, ticks) / 1_000L;
        if (hours < 1L) {
            return Text.of("gui.barnyard_romance.time.under_an_hour");
        }
        if (hours < 24L) {
            return Text.of("gui.barnyard_romance.time.hours", hours);
        }
        return Text.of("gui.barnyard_romance.time.days", hours / 24L);
    }

    public static Text ago(long ticks) {
        long seconds = Math.max(0L, ticks) / 20L;
        if (seconds < 60L) {
            return Text.of("gui.barnyard_romance.time.just_now");
        }
        long minutes = seconds / 60L;
        if (minutes < 60L) {
            return Text.of("gui.barnyard_romance.time.minutes_ago", minutes);
        }
        long hours = minutes / 60L;
        if (hours < 24L) {
            return Text.of("gui.barnyard_romance.time.hours_ago", hours);
        }
        return Text.of("gui.barnyard_romance.time.days_ago", hours / 24L);
    }

    public Text sexText() {
        return (female ? Sex.FEMALE : Sex.MALE).text();
    }

    public String healthText() {
        return hp(health) + "/" + hp(maxHealth);
    }

    public boolean showBreeding() {
        return known && (pregnant || stage != AgeStage.BABY);
    }

    // Fathers never wear out, and the young haven't bred yet, so only adult and old females show fertility.
    public boolean showFertility() {
        return known && female && (stage == AgeStage.ADULT || stage == AgeStage.OLD);
    }

    public List<InspectorRow> rows() {
        if (!known) {
            return List.of(InspectorRow.HEALTH);
        }
        List<InspectorRow> rows = new ArrayList<>(List.of(
                InspectorRow.FAMILIAR,
                InspectorRow.FEED,
                InspectorRow.GROWTH,
                InspectorRow.WEIGHT,
                InspectorRow.HEALTH
        ));
        if (showFertility()) {
            rows.add(InspectorRow.FERTILITY);
        }
        if (!milkText.isEmpty()) {
            rows.add(InspectorRow.MILK);
        }
        if (showBreeding()) {
            rows.add(InspectorRow.BREEDING);
            if (!detailText().isEmpty()) {
                rows.add(InspectorRow.DETAIL);
            }
        }
        return List.copyOf(rows);
    }

    public Text detailText() {
        return pregnant ? fatherText : lastBirthText;
    }

    private static String hp(float value) {
        if (value == (int) value) {
            return Integer.toString((int) value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
