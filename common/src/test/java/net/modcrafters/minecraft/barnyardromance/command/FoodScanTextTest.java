package net.modcrafters.minecraft.barnyardromance.command;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoodScanTextTest {
    @Test
    void summaryTellsWhereToCopyThePack() {
        assertEquals(
                "Scanned 1200 recipes: added 3 items to 1 tag. Wrote scan/here. "
                        + "Copy the barnyard_romance_scan folder into your world's datapacks and run /reload.",
                FoodScanText.summary(1200, 3, 1, "scan/here", Map.of())
        );
    }

    @Test
    void summaryListsRecipeTypesItCouldNotRead() {
        Map<String, Integer> skipped = new TreeMap<>(Map.of("minecraft:crafting", 12, "othermod:cooking_pot", 40));

        assertEquals(
                "Scanned 10 recipes: added 0 items to 0 tags. Wrote x. "
                        + "Copy the barnyard_romance_scan folder into your world's datapacks and run /reload. "
                        + "Couldn't read: minecraft:crafting (12), othermod:cooking_pot (40).",
                FoodScanText.summary(10, 0, 0, "x", skipped)
        );
    }
}
