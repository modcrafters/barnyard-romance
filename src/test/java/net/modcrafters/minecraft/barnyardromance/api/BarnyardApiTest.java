package net.modcrafters.minecraft.barnyardromance.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BarnyardApiTest {
    // Other unit tests build the features, which install the queries, so this one starts from none and puts back
    // whatever it found.
    @Test
    void animalsBeforeInstallThrows() {
        BarnyardAnimals found = installed();
        BarnyardApi.install(null);
        try {
            IllegalStateException thrown = assertThrows(IllegalStateException.class, BarnyardApi::animals);

            assertEquals("Barnyard Romance isn't loaded", thrown.getMessage());
        } finally {
            BarnyardApi.install(found);
        }
    }

    private static BarnyardAnimals installed() {
        try {
            return BarnyardApi.animals();
        } catch (IllegalStateException none) {
            return null;
        }
    }
}
