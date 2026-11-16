package net.modcrafters.minecraft.barnyardromance.look;

import com.google.gson.JsonParser;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeadPathsTest {
    private static HeadPaths parse(String json) {
        return HeadPaths.from(JsonParser.parseString(json));
    }

    @Test
    void readsFile() {
        HeadPaths file = parse("{\"entities\": [\"minecraft:llama\"], \"head\": [\"head/EMF_head/EMF_head2\", \"head\"]}");

        assertEquals(List.of("minecraft:llama"), file.entities());
        assertEquals(List.of(List.of("head", "EMF_head", "EMF_head2"), List.of("head")), file.head());
    }

    @Test
    void entitiesAreOptional() {
        assertTrue(parse("{\"head\": [\"head\"]}").entities().isEmpty());
    }

    @Test
    void headMustBeNonEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> parse("{\"head\": []}"));
        assertThrows(IllegalArgumentException.class, () -> parse("{\"head\": \"head\"}"));
        assertThrows(IllegalArgumentException.class, () -> parse("{\"entities\": [\"minecraft:cow\"]}"));
    }

    // A file that names the animal is about that animal, so it goes before the files for every animal.
    @Test
    void namedFilesComeBeforeGeneralOnes() {
        HeadPaths general = parse("{\"head\": [\"body/EMF_body/EMF_head2\", \"head\"]}");
        HeadPaths llama = parse("{\"entities\": [\"minecraft:llama\"], \"head\": [\"head/EMF_head/EMF_head2\"]}");

        assertEquals(List.of(List.of("head", "EMF_head", "EMF_head2"), List.of("body", "EMF_body", "EMF_head2"), List.of("head")),
                HeadPaths.candidates("minecraft:llama", List.of(general, llama)));
    }

    @Test
    void otherAnimalsGetOnlyGeneralOnes() {
        HeadPaths general = parse("{\"head\": [\"head\"]}");
        HeadPaths llama = parse("{\"entities\": [\"minecraft:llama\"], \"head\": [\"head/EMF_head/EMF_head2\"]}");

        assertEquals(List.of(List.of("head")), HeadPaths.candidates("minecraft:cow", List.of(general, llama)));
    }

    // Fresh Animations' head paths (through Entity Model Features) first, vanilla's head last.
    @Test
    void shippedDefaultCoversFreshAnimationsThenVanilla() throws IOException {
        String path = "assets/" + Constants.MOD_ID + "/barnyard_heads/default.json";
        try (InputStream in = HeadPathsTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(in, path + " is missing");
            HeadPaths file = HeadPaths.from(JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)));

            assertTrue(file.entities().isEmpty());
            assertEquals(List.of(
                    List.of("body", "EMF_body", "EMF_head2"),
                    List.of("body", "EMF_body", "EMF_neck2", "EMF_head2"),
                    List.of("head", "EMF_head", "EMF_head2"),
                    List.of("head")
            ), file.head());
        }
    }
}
