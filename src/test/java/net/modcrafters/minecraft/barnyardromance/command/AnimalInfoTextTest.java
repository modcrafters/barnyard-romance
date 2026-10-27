package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.text.English;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnimalInfoTextTest {
    private final SizeSettings size = new SizeSettings(0.8, 1.2, 0.1);

    @Test
    void describesSexRolledScaleAndCurrentScale() {
        String text = English.of(AnimalInfoText.format(
                "Cow",
                "minecraft:cow",
                Optional.of(new AnimalTraits(Sex.FEMALE, 0.90)),
                0.90,
                AgeStage.ADULT,
                size
        ));

        assertEquals("Cow (minecraft:cow): XL female, scale 0.9000 (attribute 0.9000), adult, familiar 0%, growth 20m 0s, weight 50%, fertility 100%", text);
    }

    @Test
    void milkIsListedWhileItFlows() {
        Optional<AnimalTraits> traits = Optional.of(new AnimalTraits(Sex.FEMALE, 0.90));

        String text = English.of(AnimalInfoText.format("Cow", "minecraft:cow", traits, 0.90, AgeStage.ADULT, Optional.empty(), size, 0L, Text.plain("2d left, 1/2 today")));

        assertEquals("Cow (minecraft:cow): XL female, scale 0.9000 (attribute 0.9000), adult, familiar 0%, growth 20m 0s, weight 50%, fertility 100%, milk 2d left, 1/2 today", text);
    }

    @Test
    void describesAnAnimalThatHasNotBeenAssignedYet() {
        String text = English.of(AnimalInfoText.format(
                "Pig",
                "minecraft:pig",
                Optional.empty(),
                1.0,
                AgeStage.BABY,
                size
        ));

        assertEquals("Pig (minecraft:pig): unassigned, attribute 1.0000, baby", text);
    }

    @Test
    void pregnancyIsAddedToTheCommandAndTheTooltip() {
        Optional<AnimalTraits> traits = Optional.of(new AnimalTraits(Sex.FEMALE, 0.72));
        Optional<Text> pregnancy = Optional.of(Text.plain("Pregnant 50% · 10m 0s"));

        assertEquals(
                "Cow (minecraft:cow): S female, scale 0.7200 (attribute 0.7200), adult, familiar 0%, growth 20m 0s, weight 50%, fertility 100%, Pregnant 50% · 10m 0s",
                English.of(AnimalInfoText.format("Cow", "minecraft:cow", traits, 0.72, AgeStage.ADULT, pregnancy, size, 0L))
        );
        assertEquals(
                List.of("Cow", "S female, adult, growth 20m 0s", "Pregnant 50% · 10m 0s"),
                AnimalInfoText.hoverLines("Cow", traits, AgeStage.ADULT, pregnancy, size).stream().map(English::of).toList()
        );
    }

    @Test
    void showsWeightAndFertilityRecoveredByNow() {
        Optional<AnimalTraits> traits = Optional.of(new AnimalTraits(Sex.FEMALE, 0.72).withWeight(0.8).withFertility(0.1, 0L));

        assertEquals(
                "Cow (minecraft:cow): S female, scale 0.7200 (attribute 0.7200), adult, familiar 0%, growth 20m 0s, weight 80%, fertility 30%",
                English.of(AnimalInfoText.format("Cow", "minecraft:cow", traits, 0.72, AgeStage.ADULT, Optional.empty(), size, 48_000L))
        );
    }

    @Test
    void malesShowWeightButNoFertility() {
        Optional<AnimalTraits> traits = Optional.of(new AnimalTraits(Sex.MALE, 1.12).withWeight(0.3));

        assertEquals(
                "Bull (minecraft:cow): S male, scale 1.1200 (attribute 1.1200), adult, familiar 0%, growth 20m 0s, weight 30%",
                English.of(AnimalInfoText.format("Bull", "minecraft:cow", traits, 1.12, AgeStage.ADULT, Optional.empty(), size, 0L))
        );
    }

    @Test
    void namesTheAgeStage() {
        Optional<AnimalTraits> traits = Optional.of(new AnimalTraits(Sex.MALE, 1.12));

        assertEquals(
                List.of("Bull", "S male, old, growth 20m 0s"),
                AnimalInfoText.hoverLines("Bull", traits, AgeStage.OLD, size).stream().map(English::of).toList()
        );
        assertEquals(
                List.of("Bull", "S male, young, growth 20m 0s"),
                AnimalInfoText.hoverLines("Bull", traits, AgeStage.YOUNG, size).stream().map(English::of).toList()
        );
    }
}
