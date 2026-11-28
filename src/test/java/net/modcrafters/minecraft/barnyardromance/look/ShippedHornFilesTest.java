package net.modcrafters.minecraft.barnyardromance.look;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.compat.Jdk;
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

    // Meadow's wooly cow: its own Highland horns, bigger on bulls and as small nubs on one cow in four, drawn from
    // Meadow's skin, whose horn pixels are the same in every variant.
    @Test
    void woolyCowBullsHaveBigHornsAndSomeCowsNubs() throws IOException {
        HornFile wooly = load("wooly_cow");

        assertEquals(List.of("meadow:wooly_cow"), wooly.entities());
        assertEquals("meadow:textures/entity/cow/highland_cattle.png", wooly.texture());
        assertEquals(128, wooly.textureWidth());
        assertEquals(128, wooly.textureHeight());
        assertEquals(0.25, wooly.femaleChance(), 1.0e-9);
        assertTrue(wooly.hide().contains(new HideRule(List.of("head", "horn_right_top_r1"), null, null)));
        assertTrue(wooly.hide().contains(new HideRule(List.of("head", "horn_left_top_r1"), null, null)));
        assertFalse(wooly.male().isEmpty());
        assertFalse(wooly.female().isEmpty());
    }

    // Meadow's head wears a wool box inflated by 0.2, so its top is at y -4.2 (y grows downward). A cow's nubs and the
    // root of a bull's bar must reach into it, not float a sliver above it.
    @Test
    void woolyCowHornsSitInTheWool() throws IOException {
        HornFile wooly = load("wooly_cow");
        double woolTop = -4.2;

        for (HornPart nub : wooly.female()) {
            HornCube cube = nub.cubes().get(0);
            double bottom = nub.pivot().y() + cube.from().y() + cube.size().y() + cube.inflate();
            assertTrue(bottom >= woolTop - 1.0e-6, "a cow's nub floats above the wool, its bottom at " + bottom);
        }
        for (HornPart horn : wooly.male()) {
            HornCube bar = horn.cubes().get(0);
            double top = horn.pivot().y() + bar.from().y() - bar.inflate();
            assertTrue(top >= woolTop - 1.0e-6, "a bull's bar stands proud of the wool at its root, its top at " + top);
        }
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
        assertEquals(new HornAttach(List.of("head"), new Xyz(-0.5f, 0, 0)), Jdk.last(goat.attach()));
        assertTrue(goat.hide().contains(new HideRule(List.of("head", "right_horn"), null, null)));
        assertTrue(goat.hide().contains(new HideRule(List.of("head", "left_horn"), null, null)));
        for (HornPart part : goat.male()) {
            assertFalse(part.side() == HornSide.ANY, "a billy's horns must each have a side, so a lost horn is not drawn");
        }
    }

    // Fresh Animations (through Entity Model Features) draws the real head as body/EMF_body/EMF_head2 and leaves
    // vanilla's head empty, so that path comes first; without it the file falls back to vanilla's head.
    @Test
    void everyFileTriesFreshAnimationsHeadFirst() throws IOException {
        for (String name : List.of("cattle", "sheep", "goat", "chicken")) {
            List<HornAttach> attach = load(name).attach();

            assertEquals(List.of("body", "EMF_body", "EMF_head2"), Jdk.first(attach).path(), name);
            assertEquals(List.of("head"), Jdk.last(attach).path(), name);
        }
    }

    @Test
    void freshAnimationsOwnHornsAndWattleAreHidden() throws IOException {
        assertTrue(load("cattle").hide().contains(new HideRule(List.of("body", "EMF_body", "EMF_head2", "EMF_horns"), null, null)));
        List<HideRule> goat = load("goat").hide();
        assertTrue(goat.contains(new HideRule(List.of("body", "EMF_body", "EMF_head2", "EMF_left_horn2"), null, null)));
        assertTrue(goat.contains(new HideRule(List.of("body", "EMF_body", "EMF_head2", "EMF_right_horn2"), null, null)));
        assertTrue(load("chicken").hide().contains(new HideRule(List.of("body", "EMF_body", "EMF_head2", "EMF_wattle"), null, null)));
    }

    @Test
    void everyCubeFitsItsTexture() throws IOException {
        for (String name : List.of("cattle", "sheep", "goat")) {
            HornFile file = load(name);
            List<HornPart> parts = new ArrayList<>(file.male());
            parts.addAll(file.female());
            while (!parts.isEmpty()) {
                HornPart part = Jdk.removeLast(parts);
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
