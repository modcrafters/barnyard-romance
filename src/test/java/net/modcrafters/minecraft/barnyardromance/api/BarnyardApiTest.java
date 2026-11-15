package net.modcrafters.minecraft.barnyardromance.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BarnyardApiTest {
    // Unit tests never start the mod, so nothing has installed the queries.
    @Test
    void animalsBeforeInstallThrows() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, BarnyardApi::animals);

        assertEquals("Barnyard Romance isn't loaded", thrown.getMessage());
    }
}
