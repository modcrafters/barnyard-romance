package net.modcrafters.minecraft.barnyardromance.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BarnyardConfigLoadTest {
    @Test
    void missingFileIsCreatedFromDefaults() throws IOException {
        Path file = Files.createTempDirectory("barnyard").resolve("barnyard_romance.json");

        BarnyardConfig.load(file);

        assertTrue(Files.exists(file));
        assertEquals(0.5, BarnyardConfig.parse(Files.readString(file)).femaleRatio(), 1.0e-9);
        assertEquals(0.5, BarnyardConfig.current().femaleRatio(), 1.0e-9);
    }

    @Test
    void brokenFileKeepsDefaultsAndLeavesTheFileAlone() throws IOException {
        Path file = Files.createTempDirectory("barnyard").resolve("barnyard_romance.json");
        Files.writeString(file, "{");

        BarnyardConfig.load(file);

        assertEquals("{", Files.readString(file));
        assertEquals(0.5, BarnyardConfig.current().femaleRatio(), 1.0e-9);
    }
}
