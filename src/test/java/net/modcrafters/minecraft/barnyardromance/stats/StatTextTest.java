package net.modcrafters.minecraft.barnyardromance.stats;

import net.modcrafters.minecraft.barnyardromance.milk.api.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.core.text.English;
import net.modcrafters.minecraft.barnyardromance.BarnyardFeatures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatTextTest {
    @BeforeAll
    static void installFeatures() {
        BarnyardFeatures.ensureInstalled();
    }

    @Test
    void fatherUsesTheLedgerWords() {
        assertEquals("Father XL bull", English.of(StatText.father("XL", "minecraft:cow")));
        assertEquals("Father M stallion", English.of(StatText.father("M", "minecraft:horse")));
        assertEquals("Father M polar bear", English.of(StatText.father("M", "minecraft:polar_bear")));
        assertEquals("Father S mate", English.of(StatText.father("S", "")));
    }

    @Test
    void eggsTextCountsTodaysEggs() {
        assertEquals("1/3 today", English.of(StatText.eggsText(1, 3, false)));
        assertEquals("None today (hungry yesterday)", English.of(StatText.eggsText(0, 0, true)));
        assertTrue(StatText.eggsText(0, 0, false).isEmpty());
    }

    @Test
    void roosterTextCountsTheEggsHisVigourCovers() {
        assertEquals("Fertile, 6 eggs left", English.of(StatText.roosterText(false, 1.0, false, 0.15)));
        assertEquals("Fertile, 1 egg left", English.of(StatText.roosterText(false, 0.15, false, 0.15)));
        assertEquals("Out of Vigour", English.of(StatText.roosterText(false, 0.1, false, 0.15)));
        assertEquals("Fertile, 2 eggs left, each costs health", English.of(StatText.roosterText(false, 0.3, true, 0.15)));
        assertEquals("Not fertile yet", English.of(StatText.roosterText(true, 1.0, false, 0.15)));
        assertEquals("Not fertile yet", English.of(StatText.roosterText(true, 0.0, true, 0.15)));
    }

    @Test
    void milkTextWhileNursing() {
        assertEquals("Flowing, 1/2 today", English.of(StatText.milkText(new MilkStatus(true, 100_000L, 1, 2))));
    }

    @Test
    void milkTextCountsDown() {
        assertEquals("2d left, 1/2 today", English.of(StatText.milkText(new MilkStatus(false, 2 * 24_000L + 500L, 1, 2))));
        assertEquals("5h left, 0/1 today", English.of(StatText.milkText(new MilkStatus(false, 5 * 1_000L + 10L, 0, 1))));
        assertEquals("<1h left, 1/1 today", English.of(StatText.milkText(new MilkStatus(false, 100L, 1, 1))));
    }

    @Test
    void breedingSaysWhatIsMissing() {
        assertEquals(StatText.NOT_TAMED, StatText.breeding(true, false, 1.0, false, false, -1, -1, 0));
        assertEquals("Not tamed", English.of(StatText.NOT_TAMED));
        assertEquals("Needs 30% familiarity", English.of(StatText.breeding(true, true, 0.2, false, false, -1, -1, 0)));
        assertEquals("Needs 30% familiarity", English.of(StatText.breeding(false, false, 0.0, false, false, -1, -1, 0)));
        assertEquals(StatText.READY, StatText.breeding(false, false, 0.3, false, false, -1, -1, 0));
        assertEquals(StatText.READY, StatText.breeding(true, true, 1.0, false, false, -1, -1, 0));
        assertEquals("Ready", English.of(StatText.READY));
    }

    @Test
    void lastBirthIsBlankUntilTheFirstBirth() {
        assertTrue(StatText.lastBirth("Mule", -1L, 1_000L).isEmpty());
        assertTrue(StatText.lastBirth("", 0L, 1_000L).isEmpty());
        assertEquals("Last birth: Mule, 3m ago", English.of(StatText.lastBirth("Mule", 1_000L, 1_000L + 3 * 60 * 20)));
    }

    @Test
    void agoRoundsDownToTheLargestUnit() {
        assertEquals("just now", English.of(StatText.ago(59 * 20)));
        assertEquals("1m ago", English.of(StatText.ago(60 * 20)));
        assertEquals("59m ago", English.of(StatText.ago(59 * 60 * 20 + 59 * 20)));
        assertEquals("2h ago", English.of(StatText.ago(2 * 60 * 60 * 20)));
        assertEquals("3d ago", English.of(StatText.ago(3L * 24 * 60 * 60 * 20)));
        assertEquals("just now", English.of(StatText.ago(-100)));
    }

    @Test
    void gameTimeCountsHoursThenDays() {
        assertEquals("<1h", English.of(StatText.gameTime(500L)));
        assertEquals("5h", English.of(StatText.gameTime(5_000L)));
        assertEquals("2d", English.of(StatText.gameTime(2 * 24_000L)));
    }

    @Test
    void breedingShowsRestAndLove() {
        assertEquals("Resting 1m 0s", English.of(StatText.breeding(false, false, 1.0, false, false, -1, 1200, 0)));
        assertEquals(StatText.IN_LOVE, StatText.breeding(false, false, 1.0, false, false, 600, -1, 0));
        assertEquals("In love", English.of(StatText.IN_LOVE));
        assertEquals("Resting 1m 0s", English.of(StatText.breeding(false, false, 1.0, false, false, 600, 1200, 0)));
    }

    @Test
    void breedingStatusExpiresWithTheClock() {
        assertEquals(StatText.READY, StatText.breeding(false, false, 1.0, false, false, 600, 1200, 1200));
    }

    @Test
    void onlyLoveAndReadyCountAsReady() {
        assertTrue(StatText.readyToBreed(StatText.READY));
        assertTrue(StatText.readyToBreed(StatText.IN_LOVE));
        assertFalse(StatText.readyToBreed(StatText.breeding(false, false, 1.0, false, false, -1, 1200, 0)));
        assertFalse(StatText.readyToBreed(StatText.NOT_TAMED));
    }

    @Test
    void aWornOutMotherSaysSo() {
        assertEquals(StatText.WORN_OUT, StatText.breeding(false, false, 1.0, false, true, -1, -1, 0));
        assertEquals("Worn out", English.of(StatText.WORN_OUT));
        assertFalse(StatText.readyToBreed(StatText.WORN_OUT));
    }

    @Test
    void aYoungAnimalIsTooYoungOnceItIsFamiliarEnough() {
        assertEquals(StatText.TOO_YOUNG, StatText.breeding(false, false, 1.0, true, false, -1, -1, 0));
        assertEquals("Too young", English.of(StatText.TOO_YOUNG));
        assertEquals("Needs 30% familiarity", English.of(StatText.breeding(false, false, 0.0, true, false, -1, -1, 0)));
        assertEquals(StatText.TOO_YOUNG, StatText.breeding(false, false, 1.0, true, false, 600, 1200, 0));
        assertFalse(StatText.readyToBreed(StatText.TOO_YOUNG));
    }

    @Test
    void restingWinsOverWornOut() {
        assertEquals("Resting 1m 0s", English.of(StatText.breeding(false, false, 1.0, false, true, -1, 1200, 0)));
    }

    @Test
    void eggRowIsLabelledForTheBird() {
        assertEquals("Rooster", English.of(StatText.eggRowLabel(false, "minecraft:chicken")));
        assertEquals("Male", English.of(StatText.eggRowLabel(false, "naturalist:duck")));
        assertEquals("Eggs", English.of(StatText.eggRowLabel(true, "naturalist:duck")));
    }
}
