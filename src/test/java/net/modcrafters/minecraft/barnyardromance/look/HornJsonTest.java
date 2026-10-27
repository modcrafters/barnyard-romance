package net.modcrafters.minecraft.barnyardromance.look;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HornJsonTest {
    private static final String FULL = """
            {
              "entities": ["minecraft:cow", "minecraft:mooshroom"],
              "attach": "head",
              "texture": "barnyard_romance:textures/entity/horns/cattle.png",
              "texture_size": [32, 32],
              "female_chance": 0.75,
              "hide": [
                "head/right_horn",
                { "part": "head", "size": [1, 3, 1] }
              ],
              "male": [
                {
                  "pivot": [-4, -3, -4], "rotation": [0, 0, -20], "side": "right", "mirror": true,
                  "cubes": [ { "from": [-3, -1, -1], "size": [3, 2, 2], "uv": [0, 0] } ],
                  "children": []
                }
              ],
              "female": []
            }
            """;

    private static HornFile parse(String json) {
        return HornJson.parse(json);
    }

    private static HornFile minimal(String extra) {
        return parse("{\"entities\": [\"minecraft:cow\"], \"texture\": \"a:b.png\", \"texture_size\": [16, 16]" + extra + "}");
    }

    @Test
    void readsFullFile() {
        HornFile file = parse(FULL);

        assertEquals(List.of("minecraft:cow", "minecraft:mooshroom"), file.entities());
        assertEquals(List.of("head"), file.attach());
        assertEquals("barnyard_romance:textures/entity/horns/cattle.png", file.texture());
        assertEquals(32, file.textureWidth());
        assertEquals(32, file.textureHeight());
        assertEquals(0.75, file.femaleChance(), 1.0e-9);
        assertEquals(2, file.hide().size());
        assertEquals(List.of("head", "right_horn"), file.hide().get(0).path());
        assertNull(file.hide().get(0).size());
        assertEquals(List.of("head"), file.hide().get(1).path());
        assertEquals(new Xyz(1, 3, 1), file.hide().get(1).size());
        assertNull(file.hide().get(1).from());
        assertEquals(2, file.male().size());
        assertTrue(file.female().isEmpty());
    }

    @Test
    void defaults() {
        HornFile file = minimal("");

        assertEquals(List.of("head"), file.attach());
        assertEquals(new Xyz(0, 0, 0), file.offset());
        assertTrue(file.male().isEmpty());
        assertTrue(file.female().isEmpty());
        assertTrue(file.hide().isEmpty());
        assertEquals(0.0, file.femaleChance(), 1.0e-9);
    }

    @Test
    void attachPathsAreSplitOnSlashes() {
        assertEquals(List.of("body", "neck", "head"), minimal(", \"attach\": \"body/neck/head\"").attach());
    }

    @Test
    void readsOffsetAndHideFrom() {
        HornFile file = minimal(", \"offset\": [-0.5, 0, 0], \"hide\": [{\"part\": \"head\", \"size\": [1, 2, 1], \"from\": [3, -5.5, -4]}]");

        assertEquals(new Xyz(-0.5f, 0, 0), file.offset());
        assertEquals(new Xyz(3, -5.5f, -4), file.hide().get(0).from());
    }

    @Test
    void mirrorAddsSwappedCopy() {
        HornFile file = minimal("""
                , "male": [{
                  "pivot": [-4, -3, -4], "rotation": [10, 20, 30], "side": "right", "mirror": true,
                  "cubes": [{"from": [-4, -1, -1], "size": [4, 2, 2], "uv": [0, 4], "inflate": -0.25}],
                  "children": [{"pivot": [-4, 0, 0], "rotation": [0, -25, 30], "cubes": [{"from": [-3, -1, -1], "size": [3, 2, 2], "uv": [0, 8]}]}]
                }]
                """);

        assertEquals(2, file.male().size());
        HornPart right = file.male().get(0);
        HornPart left = file.male().get(1);
        assertEquals(HornSide.RIGHT, right.side());
        assertEquals(HornSide.LEFT, left.side());
        assertEquals(new Xyz(4, -3, -4), left.pivot());
        assertEquals(new Xyz(10, -20, -30), left.rotation());
        HornCube cube = left.cubes().get(0);
        assertEquals(new Xyz(0, -1, -1), cube.from());
        assertEquals(new Xyz(4, 2, 2), cube.size());
        assertEquals(0, cube.u());
        assertEquals(4, cube.v());
        assertEquals(-0.25f, cube.inflate(), 1.0e-6);
        assertTrue(cube.mirror());
        assertFalse(right.cubes().get(0).mirror());
        HornPart child = left.children().get(0);
        assertEquals(new Xyz(4, 0, 0), child.pivot());
        assertEquals(new Xyz(0, 25, -30), child.rotation());
        assertEquals(new Xyz(0, -1, -1), child.cubes().get(0).from());
    }

    @Test
    void mirroringAnAnySidedPartStaysAny() {
        HornFile file = minimal(", \"female\": [{\"pivot\": [-1, 0, 0], \"mirror\": true, \"cubes\": []}]");

        assertEquals(HornSide.ANY, file.female().get(1).side());
    }

    @Test
    void rejectsBadJson() {
        assertThrows(IllegalArgumentException.class, () -> parse("[]"));
        assertThrows(IllegalArgumentException.class, () -> parse("{\"texture\": \"a:b.png\", \"texture_size\": [16, 16]}"));
        assertThrows(IllegalArgumentException.class, () -> parse("{\"entities\": [], \"texture\": \"a:b.png\", \"texture_size\": [16, 16]}"));
        assertThrows(IllegalArgumentException.class, () -> minimal(", \"male\": [{\"pivot\": [0, 0, 0], \"cubes\": [{\"from\": [0, 0, 0], \"size\": [-1, 2, 2], \"uv\": [0, 0]}]}]"));
        assertThrows(IllegalArgumentException.class, () -> minimal(", \"male\": [{\"pivot\": [0, 0, 0], \"side\": \"up\", \"cubes\": []}]"));
        assertThrows(IllegalArgumentException.class, () -> parse("{\"entities\": [\"minecraft:cow\"], \"texture\": \"a:b.png\", \"texture_size\": [16]}"));
        assertThrows(IllegalArgumentException.class, () -> parse("{\"entities\": [\"minecraft:cow\"], \"texture\": \"a:b.png\", \"texture_size\": [0, 16]}"));
        assertThrows(IllegalArgumentException.class, () -> parse("{\"entities\": [\"minecraft:cow\"], \"texture_size\": [16, 16]}"));
        assertThrows(IllegalArgumentException.class, () -> minimal(", \"hide\": [3]"));
        assertThrows(IllegalArgumentException.class, () -> minimal(", \"male\": [{\"pivot\": [0, 0], \"cubes\": []}]"));
    }

    @Test
    void clampsChance() {
        assertEquals(1.0, minimal(", \"female_chance\": 2").femaleChance(), 1.0e-9);
        assertEquals(0.0, minimal(", \"female_chance\": -1").femaleChance(), 1.0e-9);
    }
}
