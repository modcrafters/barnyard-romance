package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Fertility;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// The one-line summary the info stick prints. Its words are lower case mid-sentence, so it has its own keys.
public final class AnimalInfoText {
    private static final String SEPARATOR = ", ";

    private AnimalInfoText() {
    }

    public static Text format(
            String name,
            String speciesId,
            Optional<AnimalTraits> traits,
            double attributeScale,
            AgeStage stage,
            SizeSettings size
    ) {
        return format(name, speciesId, traits, attributeScale, stage, Optional.empty(), size, 0L);
    }

    public static Text format(
            String name,
            String speciesId,
            Optional<AnimalTraits> traits,
            double attributeScale,
            AgeStage stage,
            Optional<Text> pregnancyLine,
            SizeSettings size,
            long now
    ) {
        return format(name, speciesId, traits, attributeScale, stage, pregnancyLine, size, now, Text.EMPTY);
    }

    public static Text format(
            String name,
            String speciesId,
            Optional<AnimalTraits> traits,
            double attributeScale,
            AgeStage stage,
            Optional<Text> pregnancyLine,
            SizeSettings size,
            long now,
            Text milkText
    ) {
        String attribute = formatScale(attributeScale);
        List<Text> parts = new ArrayList<>();
        if (traits.isEmpty()) {
            parts.add(Text.of("gui.barnyard_romance.info.unassigned"));
            parts.add(Text.of("gui.barnyard_romance.info.attribute", attribute));
            parts.add(life(stage));
        } else {
            AnimalTraits value = traits.get();
            parts.add(label(value, size));
            parts.add(Text.of("gui.barnyard_romance.info.scale", formatScale(value.scale()), attribute));
            parts.add(life(stage));
            parts.add(Text.of("gui.barnyard_romance.info.familiar", Familiarity.percent(value.familiarity())));
            parts.add(Text.of("gui.barnyard_romance.info.growth", Growth.clock(value.growthTicks())));
            parts.add(Text.of("gui.barnyard_romance.info.weight", Familiarity.percent(value.weight())));
            if (value.sex() == Sex.FEMALE) {
                double fertility = Fertility.current(value, BarnyardConfig.current().ageFor(speciesId), now);
                parts.add(Text.of("gui.barnyard_romance.info.fertility", Familiarity.percent(fertility)));
            }
            if (!milkText.isEmpty()) {
                parts.add(Text.of("gui.barnyard_romance.info.milk", milkText));
            }
        }
        pregnancyLine.ifPresent(parts::add);
        return Text.of("gui.barnyard_romance.info.line", name, speciesId, Text.join(SEPARATOR, parts));
    }

    public static List<Text> hoverLines(String name, Optional<AnimalTraits> traits, AgeStage stage, SizeSettings size) {
        return hoverLines(name, traits, stage, Optional.empty(), size);
    }

    public static List<Text> hoverLines(
            String name,
            Optional<AnimalTraits> traits,
            AgeStage stage,
            Optional<Text> pregnancyLine,
            SizeSettings size
    ) {
        if (traits.isEmpty()) {
            return List.of();
        }
        AnimalTraits value = traits.get();
        Text detail = Text.join(SEPARATOR, List.of(
                label(value, size),
                life(stage),
                Text.of("gui.barnyard_romance.info.growth", Growth.clock(value.growthTicks()))
        ));
        if (pregnancyLine.isEmpty()) {
            return List.of(Text.plain(name), detail);
        }
        return List.of(Text.plain(name), detail, pregnancyLine.get());
    }

    private static Text label(AnimalTraits traits, SizeSettings size) {
        String letter = SizeClass.of(traits.sex(), traits.scale(), size).name();
        return traits.sex() == Sex.FEMALE
                ? Text.of("gui.barnyard_romance.info.female", letter)
                : Text.of("gui.barnyard_romance.info.male", letter);
    }

    private static Text life(AgeStage stage) {
        return switch (stage) {
            case BABY -> Text.of("gui.barnyard_romance.info.baby");
            case YOUNG -> Text.of("gui.barnyard_romance.info.young");
            case ADULT -> Text.of("gui.barnyard_romance.info.adult");
            case OLD -> Text.of("gui.barnyard_romance.info.old");
        };
    }

    private static String formatScale(double scale) {
        return String.format(Locale.ROOT, "%.4f", scale);
    }
}
