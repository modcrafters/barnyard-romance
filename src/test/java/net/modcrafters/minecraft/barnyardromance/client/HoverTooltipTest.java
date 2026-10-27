package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.command.AnimalInfoText;
import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.text.English;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HoverTooltipTest {
    private final SizeSettings size = new SizeSettings(0.8, 1.2, 0.1);

    @Test
    void hoverTextShowsTheSizeLetterInsteadOfADecimal() {
        List<String> lines = AnimalInfoText.hoverLines(
                "Cow",
                Optional.of(new AnimalTraits(Sex.FEMALE, 0.90)),
                AgeStage.ADULT,
                size
        ).stream().map(English::of).toList();

        assertEquals(List.of("Cow", "XL female, adult, growth 20m 0s"), lines);
    }

    @Test
    void babiesShowTheirLetterToo() {
        List<String> lines = AnimalInfoText.hoverLines(
                "Pig",
                Optional.of(new AnimalTraits(Sex.MALE, 1.12)),
                AgeStage.BABY,
                size
        ).stream().map(English::of).toList();

        assertEquals(List.of("Pig", "S male, baby, growth 20m 0s"), lines);
    }

    @Test
    void anAnimalWithoutASexShowsNothing() {
        assertTrue(AnimalInfoText.hoverLines("Sheep", Optional.empty(), AgeStage.ADULT, size).isEmpty());
    }

    @Test
    void theCenterOfTheViewLandsInTheCenterOfTheScreen() {
        assertEquals(Optional.of(new ScreenPoint(100, 50)), ClipProjection.toScreen(0.0f, 0.0f, 1.0f, 200, 100));
    }

    @Test
    void theTopLeftClipCornerLandsOnTheTopLeftPixel() {
        assertEquals(Optional.of(new ScreenPoint(0, 0)), ClipProjection.toScreen(-1.0f, 1.0f, 1.0f, 200, 100));
    }

    @Test
    void pointsBehindTheCameraAreNotShown() {
        assertTrue(ClipProjection.toScreen(0.0f, 0.0f, -0.1f, 200, 100).isEmpty());
    }

    @Test
    void pointsOutsideTheViewAreNotShown() {
        assertTrue(ClipProjection.toScreen(1.5f, 0.0f, 1.0f, 200, 100).isEmpty());
    }

    @Test
    void theTooltipSitsAboveTheAnimalAndStaysOnScreen() {
        assertEquals(new ScreenPoint(80, 54), TooltipPlacement.place(100, 80, 40, 20, 200, 100));
        assertEquals(new ScreenPoint(4, 4), TooltipPlacement.place(0, 0, 40, 20, 200, 100));
        assertEquals(new ScreenPoint(156, 69), TooltipPlacement.place(190, 95, 40, 20, 200, 100));
    }
}
