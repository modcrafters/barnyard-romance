package net.modcrafters.minecraft.barnyardromance.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientConfigTest {
    @Test
    void emptyConfigTurnsEverythingOn() {
        ClientConfig config = ClientConfig.parse("{}");

        assertTrue(config.belly());
        assertTrue(config.horns());
    }

    @Test
    void eachSwitchIsReadOnItsOwn() {
        ClientConfig config = ClientConfig.parse("{\"belly\": false}");

        assertFalse(config.belly());
        assertTrue(config.horns());
    }

    @Test
    void missingFileIsCreatedFromDefaults() throws IOException {
        Path file = Files.createTempDirectory("barnyard").resolve("barnyard_romance-client.json");

        ClientConfig.load(file);

        assertTrue(Files.exists(file));
        assertTrue(ClientConfig.parse(Files.readString(file)).horns());
        assertTrue(ClientConfig.current().belly());
    }

    @Test
    void brokenFileKeepsDefaults() throws IOException {
        Path file = Files.createTempDirectory("barnyard").resolve("barnyard_romance-client.json");
        Files.writeString(file, "{\"horns\": false");

        ClientConfig.load(file);

        assertTrue(ClientConfig.current().horns());
    }

    @Test
    void aSwitchMustBeTrueOrFalse() {
        assertThrows(IllegalArgumentException.class, () -> ClientConfig.parse("{\"horns\": \"no\"}"));
        assertThrows(IllegalArgumentException.class, () -> ClientConfig.parse("[]"));
    }
}
