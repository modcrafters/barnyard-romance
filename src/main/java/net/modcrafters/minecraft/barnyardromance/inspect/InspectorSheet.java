package net.modcrafters.minecraft.barnyardromance.inspect;

import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.stats.StatText;
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
        // A hen's eggs today, or whether a rooster is fertile today; empty for other animals.
        Text eggsText,
        boolean pregnant,
        double pregnancy,
        Text pregnancyText,
        Text fatherText,
        boolean breedingReady,
        Text breedingText,
        Text lastBirthText,
        // The highest familiarity a wild-caught animal can reach (see WildCaught); 1 for everything else.
        double cap,
        // A male's Vigour, or -1 when there is none to show, and whether he's hungry (so mating costs him health).
        double vigour,
        boolean vigourHungry
) {

    public InspectorSheet(String title, String subtitle, boolean known, boolean female, String sizeLetter, AgeStage stage, Text scaleText,
                          double familiarity, Text familiarityText, double feed, Text feedText, double growth, Text growthText, double weight,
                          Text weightText, float health, float maxHealth, double fertility, Text fertilityText, Text milkText, Text eggsText,
                          boolean pregnant, double pregnancy, Text pregnancyText, Text fatherText, boolean breedingReady, Text breedingText,
                          Text lastBirthText) {
        this(title, subtitle, known, female, sizeLetter, stage, scaleText, familiarity, familiarityText, feed, feedText, growth, growthText, weight,
                weightText, health, maxHealth, fertility, fertilityText, milkText, eggsText, pregnant, pregnancy, pregnancyText, fatherText,
                breedingReady, breedingText, lastBirthText, 1.0, -1.0, false);
    }

    public InspectorSheet withCap(double cap) {
        return new InspectorSheet(title, subtitle, known, female, sizeLetter, stage, scaleText, familiarity, familiarityText, feed, feedText, growth,
                growthText, weight, weightText, health, maxHealth, fertility, fertilityText, milkText, eggsText, pregnant, pregnancy, pregnancyText,
                fatherText, breedingReady, breedingText, lastBirthText, cap, vigour, vigourHungry);
    }

    public InspectorSheet withVigour(double vigour, boolean hungry) {
        return new InspectorSheet(title, subtitle, known, female, sizeLetter, stage, scaleText, familiarity, familiarityText, feed, feedText, growth,
                growthText, weight, weightText, health, maxHealth, fertility, fertilityText, milkText, eggsText, pregnant, pregnancy, pregnancyText,
                fatherText, breedingReady, breedingText, lastBirthText, cap, vigour, hungry);
    }

    public Text vigourText() {
        return Familiarity.percent(vigour);
    }

    public boolean showVigour() {
        return known && !female && vigour >= 0.0 && (stage == AgeStage.ADULT || stage == AgeStage.OLD);
    }

    // Empty unless the animal is wild-caught, so a familiarity stuck at its cap reads as a rule, not a bug.
    public Text wildText() {
        return known && cap < 1.0 ? Text.of("gui.barnyard_romance.inspector.wild_caught", Familiarity.percent(cap)) : Text.EMPTY;
    }

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
            return StatText.READY;
        }
        return Growth.clock(wait);
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
        List<InspectorRow> rows = new ArrayList<>(List.of(InspectorRow.FAMILIAR));
        if (!wildText().isEmpty()) {
            rows.add(InspectorRow.WILD);
        }
        rows.addAll(List.of(
                InspectorRow.FEED,
                InspectorRow.GROWTH,
                InspectorRow.WEIGHT,
                InspectorRow.HEALTH
        ));
        if (showFertility()) {
            rows.add(InspectorRow.FERTILITY);
        }
        if (showVigour()) {
            rows.add(InspectorRow.VIGOUR);
            if (vigourHungry) {
                rows.add(InspectorRow.VIGOUR_HUNGRY);
            }
        }
        if (!milkText.isEmpty()) {
            rows.add(InspectorRow.MILK);
        }
        if (!eggsText.isEmpty()) {
            rows.add(InspectorRow.EGGS);
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
