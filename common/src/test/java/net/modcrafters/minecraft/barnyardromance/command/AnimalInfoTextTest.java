package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnimalInfoTextTest {
    private final SizeSettings size = new SizeSettings(0.8, 1.2, 0.1);

    @Test
    void describesSexRolledScaleAndCurrentScale() {
        String text = AnimalInfoText.format(
                "Cow",
                "minecraft:cow",
                Optional.of(new AnimalTraits(Sex.FEMALE, 0.90)),
                0.90,
                false,
                size
        );

        assertEquals("Cow (minecraft:cow): XL female, scale 0.9000 (attribute 0.9000), adult", text);
    }

    @Test
    void describesAnAnimalThatHasNotBeenAssignedYet() {
        String text = AnimalInfoText.format(
                "Pig",
                "minecraft:pig",
                Optional.empty(),
                1.0,
                true,
                size
        );

        assertEquals("Pig (minecraft:pig): unassigned, attribute 1.0000, baby", text);
    }

    @Test
    void pregnancyIsAddedToTheCommandAndTheTooltip() {
        Optional<AnimalTraits> traits = Optional.of(new AnimalTraits(Sex.FEMALE, 0.72));
        Optional<String> pregnancy = Optional.of("Pregnant 50% · 10m 0s");

        assertEquals(
                "Cow (minecraft:cow): S female, scale 0.7200 (attribute 0.7200), adult, Pregnant 50% · 10m 0s",
                AnimalInfoText.format("Cow", "minecraft:cow", traits, 0.72, false, pregnancy, size)
        );
        assertEquals(
                List.of("Cow", "S female, adult", "Pregnant 50% · 10m 0s"),
                AnimalInfoText.hoverLines("Cow", traits, false, pregnancy, size)
        );
    }
}
