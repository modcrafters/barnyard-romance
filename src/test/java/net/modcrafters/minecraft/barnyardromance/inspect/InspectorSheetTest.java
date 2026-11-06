package net.modcrafters.minecraft.barnyardromance.inspect;

import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.milk.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.stats.StatText;
import net.modcrafters.minecraft.barnyardromance.text.English;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.inspect.InspectorRow.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InspectorSheetTest {
    @Test
    void aNamedAnimalKeepsItsNameAboveTheType() {
        InspectorSheet sheet = InspectorSheet.unknown("Bessie", "Cow", true, false, 8.0F, 10.0F);

        assertEquals("Bessie", sheet.title());
        assertEquals("Cow", sheet.subtitle());
        assertEquals("8/10", sheet.healthText());
    }

    @Test
    void feedAndScaleUseTheLedgerWords() {
        assertEquals("Ready", English.of(InspectorSheet.feedText(-1L, 0L)));
        assertEquals("15m 0s", English.of(InspectorSheet.feedText(0L, 0L)));
        assertEquals("scale 1.24", English.of(InspectorSheet.scaleText(1.24)));
    }

    @Test
    void sexNamesComeFromTheLangFile() {
        assertEquals("Female", English.of(sheet(true, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).sexText()));
        assertEquals("Male", English.of(sheet(false, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).sexText()));
    }

    private static InspectorSheet sheet(boolean female, AgeStage stage, boolean pregnant, Text father, Text lastBirth) {
        return sheet(female, stage, pregnant, father, lastBirth, Text.EMPTY);
    }

    private static InspectorSheet sheet(boolean female, AgeStage stage, boolean pregnant, Text father, Text lastBirth, Text milk) {
        return sheet(female, stage, pregnant, father, lastBirth, milk, Text.EMPTY);
    }

    private static InspectorSheet sheet(boolean female, AgeStage stage, boolean pregnant, Text father, Text lastBirth, Text milk, Text eggs) {
        return new InspectorSheet(
                "Cow", "", true, female, "M", stage, InspectorSheet.scaleText(0.9),
                0.4, Familiarity.percent(0.4), Familiarity.feedReady(0L, 0L), Text.plain("20m 0s"), 0.4, Text.plain("20m 0s"),
                0.5, Familiarity.percent(0.5), 8.0F, 10.0F, 1.0, Familiarity.percent(1.0), milk, eggs, pregnant, 0.5,
                Text.plain("10m 0s"), father, true, StatText.READY, lastBirth
        );
    }

    @Test
    void aWildCaughtAnimalGetsARowUnderFamiliar() {
        InspectorSheet wild = sheet(true, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).withCap(0.4);

        assertEquals(List.of(FAMILIAR, WILD, FEED, GROWTH, WEIGHT, HEALTH, FERTILITY, BREEDING), wild.rows());
        assertEquals("Wild-caught, max 40%", English.of(wild.wildText()));
        assertFalse(sheet(true, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).rows().contains(WILD));
    }

    @Test
    void aHungryMaleGetsANoteUnderVigour() {
        InspectorSheet hungry = sheet(false, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).withVigour(0.6, true);

        assertEquals(List.of(FAMILIAR, FEED, GROWTH, WEIGHT, HEALTH, VIGOUR, VIGOUR_HUNGRY, BREEDING), hungry.rows());
        assertEquals("60%", English.of(hungry.vigourText()));
        assertEquals("Hungry: mating costs health", English.of(StatText.VIGOUR_HUNGRY_TEXT));
    }

    @Test
    void grownMalesGetAVigourRow() {
        InspectorSheet male = sheet(false, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).withVigour(0.6, false);

        assertEquals(List.of(FAMILIAR, FEED, GROWTH, WEIGHT, HEALTH, VIGOUR, BREEDING), male.rows());
        assertTrue(sheet(false, AgeStage.OLD, false, Text.EMPTY, Text.EMPTY).withVigour(0.6, false).rows().contains(VIGOUR));
        assertFalse(sheet(false, AgeStage.YOUNG, false, Text.EMPTY, Text.EMPTY).withVigour(0.6, true).rows().contains(VIGOUR));
        assertFalse(sheet(false, AgeStage.YOUNG, false, Text.EMPTY, Text.EMPTY).withVigour(0.6, true).rows().contains(VIGOUR_HUNGRY));
        assertFalse(sheet(true, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).withVigour(0.6, false).rows().contains(VIGOUR));
        assertFalse(sheet(false, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).rows().contains(VIGOUR));
    }

    @Test
    void eggsRowSitsWhereMilkWould() {
        assertEquals(List.of(FAMILIAR, FEED, GROWTH, WEIGHT, HEALTH, FERTILITY, EGGS, BREEDING),
                sheet(true, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY, Text.EMPTY, StatText.eggsText(0, 2, false)).rows());
        assertFalse(sheet(true, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).rows().contains(EGGS));
    }

    @Test
    void noMilkRowForMales() {
        assertFalse(sheet(false, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).rows().contains(MILK));
        assertEquals(List.of(FAMILIAR, FEED, GROWTH, WEIGHT, HEALTH, FERTILITY, MILK, BREEDING),
                sheet(true, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY, StatText.milkText(new MilkStatus(true, 1L, 1, 2))).rows());
    }

    @Test
    void breedingIsShownForAdultsOfBothSexesButNotForBabies() {
        assertTrue(sheet(true, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).showBreeding());
        assertTrue(sheet(false, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).showBreeding());
        assertFalse(sheet(true, AgeStage.BABY, false, Text.EMPTY, Text.EMPTY).showBreeding());
        assertTrue(sheet(true, AgeStage.YOUNG, false, Text.EMPTY, Text.EMPTY).showBreeding());
        assertTrue(sheet(false, AgeStage.OLD, false, Text.EMPTY, Text.EMPTY).showBreeding());
    }

    @Test
    void theLineUnderBreedingShowsTheFatherWhilePregnantAndTheLastBirthAfter() {
        Text father = StatText.father("M", "minecraft:donkey");
        Text lastBirth = StatText.lastBirth("Mule", 0L, 3 * 60 * 20);
        assertEquals("Father M jack", English.of(sheet(true, AgeStage.ADULT, true, father, lastBirth).detailText()));
        assertEquals("Last birth: Mule, 3m ago", English.of(sheet(true, AgeStage.ADULT, false, Text.EMPTY, lastBirth).detailText()));
    }

    @Test
    void fertilityIsShownForAdultAndOldFemalesOnly() {
        assertTrue(sheet(true, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).showFertility());
        assertTrue(sheet(true, AgeStage.OLD, false, Text.EMPTY, Text.EMPTY).showFertility());
        assertFalse(sheet(true, AgeStage.YOUNG, false, Text.EMPTY, Text.EMPTY).showFertility());
        assertFalse(sheet(false, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).showFertility());
        assertFalse(sheet(true, AgeStage.BABY, false, Text.EMPTY, Text.EMPTY).showFertility());
    }

    @Test
    void aBullListsNoFertilityRow() {
        assertEquals(List.of(FAMILIAR, FEED, GROWTH, WEIGHT, HEALTH, BREEDING), sheet(false, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).rows());
    }

    @Test
    void aPregnantCowListsFertilityBreedingAndTheFather() {
        assertEquals(List.of(FAMILIAR, FEED, GROWTH, WEIGHT, HEALTH, FERTILITY, BREEDING, DETAIL),
                sheet(true, AgeStage.ADULT, true, StatText.father("M", "minecraft:cow"), Text.EMPTY).rows());
    }

    @Test
    void aCalfStopsAtHealth() {
        assertEquals(List.of(FAMILIAR, FEED, GROWTH, WEIGHT, HEALTH), sheet(true, AgeStage.BABY, false, Text.EMPTY, Text.EMPTY).rows());
    }

    @Test
    void anAnimalWithoutTraitsOnlyListsHealth() {
        assertEquals(List.of(HEALTH), InspectorSheet.unknown("Cow", "Cow", false, false, 10.0F, 10.0F).rows());
    }

}
