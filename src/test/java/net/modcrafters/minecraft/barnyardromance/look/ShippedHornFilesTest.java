package net.modcrafters.minecraft.barnyardromance.look;

import net.modcrafters.minecraft.barnyardromance.Constants;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShippedHornFilesTest {
    private static HornFile load(String name) throws IOException {
        String path = "assets/" + Constants.MOD_ID + "/barnyard_horns/" + name + ".json";
        try (InputStream in = ShippedHornFilesTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(in, path + " is missing");
            return HornJson.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void cattleCoversCowsAndMooshrooms() throws IOException {
        HornFile cattle = load("cattle");

        assertEquals(List.of("minecraft:cow", "minecraft:mooshroom"), cattle.entities());
        assertEquals(0.75, cattle.femaleChance(), 1.0e-9);
        assertFalse(cattle.male().isEmpty());
        assertFalse(cattle.female().isEmpty());
    }

    // Only roosters wear a comb, so a comb at a glance means rooster; hens keep vanilla's head.
    @Test
    void chickenCombIsRoostersOnly() throws IOException {
        HornFile chicken = load("chicken");

        assertEquals(List.of("minecraft:chicken"), chicken.entities());
        assertEquals(0.0, chicken.femaleChance(), 1.0e-9);
        assertFalse(chicken.male().isEmpty());
        assertTrue(chicken.female().isEmpty());
    }

    // The rooster's bigger wattles replace vanilla's, which is on the head on 26.3 and beside it on 1.21.1.
    @Test
    void chickenHidesVanillaWattlesOnEveryVersion() throws IOException {
        List<HideRule> hide = load("chicken").hide();

        assertTrue(hide.contains(new HideRule(List.of("head", "red_thing"), null, null)));
        assertTrue(hide.contains(new HideRule(List.of("red_thing"), null, null)));
    }

    @Test
    void cattleHidesEveryVanillaCowHorn() throws IOException {
        List<HideRule> hide = load("cattle").hide();

        assertTrue(hide.contains(new HideRule(List.of("head", "right_horn"), null, null)));
        assertTrue(hide.contains(new HideRule(List.of("head", "left_horn"), null, null)));
        assertTrue(hide.contains(new HideRule(List.of("head"), new Xyz(1, 3, 1), null)));
        assertTrue(hide.contains(new HideRule(List.of("head"), new Xyz(4, 2, 2), null)));
        assertTrue(hide.contains(new HideRule(List.of("head"), new Xyz(2, 2, 2), null)));
        assertTrue(hide.contains(new HideRule(List.of("head"), new Xyz(1, 2, 1), null)));
    }

    @Test
    void sheepHaveRamsAndSomeEwesWithHorns() throws IOException {
        HornFile sheep = load("sheep");

        assertEquals(List.of("minecraft:sheep"), sheep.entities());
        assertEquals(0.25, sheep.femaleChance(), 1.0e-9);
        assertTrue(sheep.hide().isEmpty());
        assertFalse(sheep.male().isEmpty());
        assertFalse(sheep.female().isEmpty());
    }

    @Test
    void goatsHideVanillaHornsAndKeepSides() throws IOException {
        HornFile goat = load("goat");

        assertEquals(List.of("minecraft:goat"), goat.entities());
        assertEquals(0.8, goat.femaleChance(), 1.0e-9);
        assertEquals(new Xyz(-0.5f, 0, 0), goat.offset());
        assertTrue(goat.hide().contains(new HideRule(List.of("head", "right_horn"), null, null)));
        assertTrue(goat.hide().contains(new HideRule(List.of("head", "left_horn"), null, null)));
        for (HornPart part : goat.male()) {
            assertFalse(part.side() == HornSide.ANY, "a billy's horns must each have a side, so a lost horn is not drawn");
        }
    }

    @Test
    void everyCubeFitsItsTexture() throws IOException {
        for (String name : List.of("cattle", "sheep", "goat")) {
            HornFile file = load(name);
            List<HornPart> parts = new ArrayList<>(file.male());
            parts.addAll(file.female());
            while (!parts.isEmpty()) {
                HornPart part = parts.removeLast();
                parts.addAll(part.children());
                for (HornCube cube : part.cubes()) {
                    // A box's texture is 2 * (depth + width) wide and depth + height tall.
                    float width = 2.0f * (cube.size().z() + cube.size().x());
                    float height = cube.size().z() + cube.size().y();
                    assertTrue(cube.u() + width <= file.textureWidth() && cube.v() + height <= file.textureHeight(),
                            name + " cube at uv " + cube.u() + "," + cube.v() + " runs off the texture");
                }
            }
        }
    }
}
