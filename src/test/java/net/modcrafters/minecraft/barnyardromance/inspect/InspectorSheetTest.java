package net.modcrafters.minecraft.barnyardromance.inspect;

import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.milk.MilkStatus;
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
    void feedAndScaleAndFatherUseTheLedgerWords() {
        assertEquals("Ready", English.of(InspectorSheet.feedText(-1L, 0L)));
        assertEquals("15m 0s", English.of(InspectorSheet.feedText(0L, 0L)));
        assertEquals("scale 1.24", English.of(InspectorSheet.scaleText(1.24)));
        assertEquals("Father XL bull", English.of(InspectorSheet.father("XL", "minecraft:cow")));
        assertEquals("Father M stallion", English.of(InspectorSheet.father("M", "minecraft:horse")));
        assertEquals("Father M polar bear", English.of(InspectorSheet.father("M", "minecraft:polar_bear")));
        assertEquals("Father S mate", English.of(InspectorSheet.father("S", "")));
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
                Text.plain("10m 0s"), father, true, InspectorSheet.READY, lastBirth
        );
    }

    @Test
    void eggsTextCountsTodaysEggs() {
        assertEquals("1/3 today", English.of(InspectorSheet.eggsText(1, 3, false)));
        assertEquals("None today (hungry yesterday)", English.of(InspectorSheet.eggsText(0, 0, true)));
        assertTrue(InspectorSheet.eggsText(0, 0, false).isEmpty());
    }

    @Test
    void roosterTextSaysWhetherHeIsFertile() {
        assertEquals("Fertile today", English.of(InspectorSheet.roosterText(false)));
        assertEquals("Not today (hungry yesterday)", English.of(InspectorSheet.roosterText(true)));
    }

    @Test
    void eggsRowSitsWhereMilkWould() {
        assertEquals(List.of(FAMILIAR, FEED, GROWTH, WEIGHT, HEALTH, FERTILITY, EGGS, BREEDING),
                sheet(true, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY, Text.EMPTY, InspectorSheet.eggsText(0, 2, false)).rows());
        assertFalse(sheet(true, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).rows().contains(EGGS));
    }

    @Test
    void milkTextWhileNursing() {
        assertEquals("Flowing, 1/2 today", English.of(InspectorSheet.milkText(new MilkStatus(true, 100_000L, 1, 2))));
    }

    @Test
    void milkTextCountsDown() {
        assertEquals("2d left, 1/2 today", English.of(InspectorSheet.milkText(new MilkStatus(false, 2 * 24_000L + 500L, 1, 2))));
        assertEquals("5h left, 0/1 today", English.of(InspectorSheet.milkText(new MilkStatus(false, 5 * 1_000L + 10L, 0, 1))));
        assertEquals("<1h left, 1/1 today", English.of(InspectorSheet.milkText(new MilkStatus(false, 100L, 1, 1))));
    }

    @Test
    void noMilkRowForMales() {
        assertFalse(sheet(false, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY).rows().contains(MILK));
        assertEquals(List.of(FAMILIAR, FEED, GROWTH, WEIGHT, HEALTH, FERTILITY, MILK, BREEDING),
                sheet(true, AgeStage.ADULT, false, Text.EMPTY, Text.EMPTY, InspectorSheet.milkText(new MilkStatus(true, 1L, 1, 2))).rows());
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
        Text father = InspectorSheet.father("M", "minecraft:donkey");
        Text lastBirth = InspectorSheet.lastBirth("Mule", 0L, 3 * 60 * 20);
        assertEquals("Father M jack", English.of(sheet(true, AgeStage.ADULT, true, father, lastBirth).detailText()));
        assertEquals("Last birth: Mule, 3m ago", English.of(sheet(true, AgeStage.ADULT, false, Text.EMPTY, lastBirth).detailText()));
    }

    @Test
    void breedingSaysWhatIsMissing() {
        assertEquals(InspectorSheet.NOT_TAMED, InspectorSheet.breeding(true, false, 1.0, false, false, -1, -1, 0));
        assertEquals("Not tamed", English.of(InspectorSheet.NOT_TAMED));
        assertEquals("Needs 30% familiarity", English.of(InspectorSheet.breeding(true, true, 0.2, false, false, -1, -1, 0)));
        assertEquals("Needs 30% familiarity", English.of(InspectorSheet.breeding(false, false, 0.0, false, false, -1, -1, 0)));
        assertEquals(InspectorSheet.READY, InspectorSheet.breeding(false, false, 0.3, false, false, -1, -1, 0));
        assertEquals(InspectorSheet.READY, InspectorSheet.breeding(true, true, 1.0, false, false, -1, -1, 0));
        assertEquals("Ready", English.of(InspectorSheet.READY));
    }

    @Test
    void lastBirthIsBlankUntilTheFirstBirth() {
        assertTrue(InspectorSheet.lastBirth("Mule", -1L, 1_000L).isEmpty());
        assertTrue(InspectorSheet.lastBirth("", 0L, 1_000L).isEmpty());
        assertEquals("Last birth: Mule, 3m ago", English.of(InspectorSheet.lastBirth("Mule", 1_000L, 1_000L + 3 * 60 * 20)));
    }

    @Test
    void agoRoundsDownToTheLargestUnit() {
        assertEquals("just now", English.of(InspectorSheet.ago(59 * 20)));
        assertEquals("1m ago", English.of(InspectorSheet.ago(60 * 20)));
        assertEquals("59m ago", English.of(InspectorSheet.ago(59 * 60 * 20 + 59 * 20)));
        assertEquals("2h ago", English.of(InspectorSheet.ago(2 * 60 * 60 * 20)));
        assertEquals("3d ago", English.of(InspectorSheet.ago(3L * 24 * 60 * 60 * 20)));
        assertEquals("just now", English.of(InspectorSheet.ago(-100)));
    }

    @Test
    void breedingShowsRestAndLove() {
        assertEquals("Resting 1m 0s", English.of(InspectorSheet.breeding(false, false, 1.0, false, false, -1, 1200, 0)));
        assertEquals(InspectorSheet.IN_LOVE, InspectorSheet.breeding(false, false, 1.0, false, false, 600, -1, 0));
        assertEquals("In love", English.of(InspectorSheet.IN_LOVE));
        assertEquals("Resting 1m 0s", English.of(InspectorSheet.breeding(false, false, 1.0, false, false, 600, 1200, 0)));
    }

    @Test
    void breedingStatusExpiresWithTheClock() {
        assertEquals(InspectorSheet.READY, InspectorSheet.breeding(false, false, 1.0, false, false, 600, 1200, 1200));
    }

    @Test
    void onlyLoveAndReadyCountAsReady() {
        assertTrue(InspectorSheet.readyToBreed(InspectorSheet.READY));
        assertTrue(InspectorSheet.readyToBreed(InspectorSheet.IN_LOVE));
        assertFalse(InspectorSheet.readyToBreed(InspectorSheet.breeding(false, false, 1.0, false, false, -1, 1200, 0)));
        assertFalse(InspectorSheet.readyToBreed(InspectorSheet.NOT_TAMED));
    }

    @Test
    void aWornOutMotherSaysSo() {
        assertEquals(InspectorSheet.WORN_OUT, InspectorSheet.breeding(false, false, 1.0, false, true, -1, -1, 0));
        assertEquals("Worn out", English.of(InspectorSheet.WORN_OUT));
        assertFalse(InspectorSheet.readyToBreed(InspectorSheet.WORN_OUT));
    }

    @Test
    void aYoungAnimalIsTooYoungOnceItIsFamiliarEnough() {
        assertEquals(InspectorSheet.TOO_YOUNG, InspectorSheet.breeding(false, false, 1.0, true, false, -1, -1, 0));
        assertEquals("Too young", English.of(InspectorSheet.TOO_YOUNG));
        assertEquals("Needs 30% familiarity", English.of(InspectorSheet.breeding(false, false, 0.0, true, false, -1, -1, 0)));
        assertEquals(InspectorSheet.TOO_YOUNG, InspectorSheet.breeding(false, false, 1.0, true, false, 600, 1200, 0));
        assertFalse(InspectorSheet.readyToBreed(InspectorSheet.TOO_YOUNG));
    }

    @Test
    void restingWinsOverWornOut() {
        assertEquals("Resting 1m 0s", English.of(InspectorSheet.breeding(false, false, 1.0, false, true, -1, 1200, 0)));
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
                sheet(true, AgeStage.ADULT, true, InspectorSheet.father("M", "minecraft:cow"), Text.EMPTY).rows());
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
