package net.modcrafters.minecraft.barnyardromance.inspect;

import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import org.junit.jupiter.api.Test;

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
        assertEquals("Ready", InspectorSheet.feedText(-1L, 0L));
        assertEquals("20m 0s", InspectorSheet.feedText(0L, 0L));
        assertEquals("scale 1.24", InspectorSheet.scaleText(1.24));
        assertEquals("Father XL bull", InspectorSheet.father("XL", "minecraft:cow"));
        assertEquals("Father M stallion", InspectorSheet.father("M", "minecraft:horse"));
    }

    private static InspectorSheet sheet(boolean female, String life, boolean pregnant, String father, String lastBirth) {
        return new InspectorSheet(
                "Cow", "", true, female, "M", life, "scale 0.90",
                0.4, "40%", Familiarity.feedReady(0L, 0L), "20m 0s", 0.4, "20m 0s",
                8.0F, 10.0F, pregnant, 0.5, "10m 0s", father,
                true, InspectorSheet.READY, lastBirth
        );
    }

    @Test
    void breedingIsShownForAdultsOfBothSexesButNotForBabies() {
        assertTrue(sheet(true, "Adult", false, "", "").showBreeding());
        assertTrue(sheet(false, "Adult", false, "", "").showBreeding());
        assertFalse(sheet(true, "Baby", false, "", "").showBreeding());
    }

    @Test
    void theLineUnderBreedingShowsTheFatherWhilePregnantAndTheLastBirthAfter() {
        assertEquals("Father M jack", sheet(true, "Adult", true, "Father M jack", "Last birth: Mule, 3m ago").detailText());
        assertEquals("Last birth: Mule, 3m ago", sheet(true, "Adult", false, "", "Last birth: Mule, 3m ago").detailText());
    }

    @Test
    void breedingSaysWhatIsMissing() {
        assertEquals(InspectorSheet.NOT_TAMED, InspectorSheet.breeding(true, false, 1.0));
        assertEquals("Needs 30% familiarity", InspectorSheet.breeding(true, true, 0.2));
        assertEquals("Needs 30% familiarity", InspectorSheet.breeding(false, false, 0.0));
        assertEquals(InspectorSheet.READY, InspectorSheet.breeding(false, false, 0.3));
        assertEquals(InspectorSheet.READY, InspectorSheet.breeding(true, true, 1.0));
    }

    @Test
    void lastBirthIsBlankUntilTheFirstBirth() {
        assertEquals("", InspectorSheet.lastBirth("Mule", -1L, 1_000L));
        assertEquals("", InspectorSheet.lastBirth("", 0L, 1_000L));
        assertEquals("Last birth: Mule, 3m ago", InspectorSheet.lastBirth("Mule", 1_000L, 1_000L + 3 * 60 * 20));
    }

    @Test
    void agoRoundsDownToTheLargestUnit() {
        assertEquals("just now", InspectorSheet.ago(59 * 20));
        assertEquals("1m ago", InspectorSheet.ago(60 * 20));
        assertEquals("59m ago", InspectorSheet.ago(59 * 60 * 20 + 59 * 20));
        assertEquals("2h ago", InspectorSheet.ago(2 * 60 * 60 * 20));
        assertEquals("3d ago", InspectorSheet.ago(3L * 24 * 60 * 60 * 20));
        assertEquals("just now", InspectorSheet.ago(-100));
    }
}
