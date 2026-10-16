package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
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
            boolean baby,
            SizeSettings size
    ) {
        return format(name, speciesId, traits, attributeScale, baby, Optional.empty(), size);
    }

    public static String format(
            String name,
            String speciesId,
            Optional<AnimalTraits> traits,
            double attributeScale,
            boolean baby,
            Optional<String> pregnancyLine,
            SizeSettings size
    ) {
        String life = baby ? "baby" : "adult";
        String attribute = formatScale(attributeScale);
        String text;
        if (traits.isEmpty()) {
            text = name + " (" + speciesId + "): unassigned, attribute " + attribute + ", " + life;
        } else {
            AnimalTraits value = traits.get();
            text = name + " (" + speciesId + "): " + label(value, size)
                    + ", scale " + formatScale(value.scale())
                    + " (attribute " + attribute + "), "
                    + life;
        }
        if (pregnancyLine.isEmpty()) {
            return text;
        }
        return text + ", " + pregnancyLine.get();
    }

    public static List<String> hoverLines(String name, Optional<AnimalTraits> traits, boolean baby, SizeSettings size) {
        return hoverLines(name, traits, baby, Optional.empty(), size);
    }

    public static List<String> hoverLines(
            String name,
            Optional<AnimalTraits> traits,
            boolean baby,
            Optional<String> pregnancyLine,
            SizeSettings size
    ) {
        if (traits.isEmpty()) {
            return List.of();
        }
        AnimalTraits value = traits.get();
        String life = baby ? "baby" : "adult";
        String detail = label(value, size) + ", " + life;
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
