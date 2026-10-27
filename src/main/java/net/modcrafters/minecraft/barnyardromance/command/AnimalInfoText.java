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

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class AnimalInfoText {
    private AnimalInfoText() {
    }

    public static String format(
            String name,
            String speciesId,
            Optional<AnimalTraits> traits,
            double attributeScale,
            AgeStage stage,
            SizeSettings size
    ) {
        return format(name, speciesId, traits, attributeScale, stage, Optional.empty(), size, 0L);
    }

    public static String format(
            String name,
            String speciesId,
            Optional<AnimalTraits> traits,
            double attributeScale,
            AgeStage stage,
            Optional<String> pregnancyLine,
            SizeSettings size,
            long now
    ) {
        return format(name, speciesId, traits, attributeScale, stage, pregnancyLine, size, now, "");
    }

    public static String format(
            String name,
            String speciesId,
            Optional<AnimalTraits> traits,
            double attributeScale,
            AgeStage stage,
            Optional<String> pregnancyLine,
            SizeSettings size,
            long now,
            String milkText
    ) {
        String life = stage.label().toLowerCase(Locale.ROOT);
        String attribute = formatScale(attributeScale);
        String text;
        if (traits.isEmpty()) {
            text = name + " (" + speciesId + "): unassigned, attribute " + attribute + ", " + life;
        } else {
            AnimalTraits value = traits.get();
            text = name + " (" + speciesId + "): " + label(value, size)
                    + ", scale " + formatScale(value.scale())
                    + " (attribute " + attribute + "), "
                    + life
                    + ", familiar " + Familiarity.percent(value.familiarity())
                    + ", growth " + Growth.clock(value.growthTicks())
                    + ", weight " + Familiarity.percent(value.weight());
            if (value.sex() == Sex.FEMALE) {
                text += ", fertility " + Familiarity.percent(Fertility.current(value, BarnyardConfig.current().ageFor(speciesId), now));
            }
            if (!milkText.isEmpty()) {
                text += ", milk " + milkText;
            }
        }
        if (pregnancyLine.isEmpty()) {
            return text;
        }
        return text + ", " + pregnancyLine.get();
    }

    public static List<String> hoverLines(String name, Optional<AnimalTraits> traits, AgeStage stage, SizeSettings size) {
        return hoverLines(name, traits, stage, Optional.empty(), size);
    }

    public static List<String> hoverLines(
            String name,
            Optional<AnimalTraits> traits,
            AgeStage stage,
            Optional<String> pregnancyLine,
            SizeSettings size
    ) {
        if (traits.isEmpty()) {
            return List.of();
        }
        AnimalTraits value = traits.get();
        String life = stage.label().toLowerCase(Locale.ROOT);
        String detail = label(value, size) + ", " + life + ", growth " + Growth.clock(value.growthTicks());
        if (pregnancyLine.isEmpty()) {
            return List.of(name, detail);
        }
        return List.of(name, detail, pregnancyLine.get());
    }

    private static String label(AnimalTraits traits, SizeSettings size) {
        return SizeClass.of(traits.sex(), traits.scale(), size).name()
                + " "
                + traits.sex().name().toLowerCase(Locale.ROOT);
    }

    private static String formatScale(double scale) {
        return String.format(Locale.ROOT, "%.4f", scale);
    }
}
