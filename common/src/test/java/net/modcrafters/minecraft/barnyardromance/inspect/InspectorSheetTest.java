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

    @Test
    void pregnancyIsShownForAnAdultFemaleAndHiddenForAMale() {
        InspectorSheet female = new InspectorSheet(
                "Cow", "", true, true, "XL", "Adult", "scale 0.90",
                0.4, "40%", 1.0, "Ready", 0.4, "20m 0s",
                8.0F, 10.0F, false, 0.0, "", ""
        );
        InspectorSheet male = new InspectorSheet(
                "Cow", "", true, false, "S", "Adult", "scale 1.10",
                0.0, "0%", Familiarity.feedReady(0L, 0L), "20m 0s", 0.4, "20m 0s",
                10.0F, 10.0F, false, 0.0, "", ""
        );

        assertTrue(female.showPregnancy());
        assertFalse(male.showPregnancy());
    }
}
