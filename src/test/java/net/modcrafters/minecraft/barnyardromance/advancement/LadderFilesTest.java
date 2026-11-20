package net.modcrafters.minecraft.barnyardromance.advancement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// The ladder advancement files: each loads only while one of its species is registered and tracked (the
// barnyard_romance:species_tracked condition, on both loaders), and each step of a ladder carries the same condition.
class LadderFilesTest {
    private static final String LADDER_DIR = "src/main/resources/data/barnyard_romance/advancement/barnyard/ladder";
    private static final String LANG = "src/main/resources/assets/barnyard_romance/lang/en_us.json";
    // Every species the allow-list can put on a ladder.
    private static final List<String> SPECIES = List.of(
            "minecraft:cow", "minecraft:mooshroom", "minecraft:sheep", "minecraft:goat", "minecraft:pig", "minecraft:chicken",
            "minecraft:rabbit", "minecraft:horse", "minecraft:donkey", "minecraft:mule", "minecraft:llama",
            "minecraft:trader_llama", "minecraft:camel",
            "naturalist:deer", "naturalist:boar", "naturalist:duck", "naturalist:turkey", "naturalist:ostrich",
            "untitledduckmod:duck", "untitledduckmod:goose", "friendsandfoes:moobloom", "meadow:water_buffalo",
            "meadow:wooly_cow", "autumnity:turkey", "environmental:duck", "environmental:yak");

    @Test
    void everyLadderHasBothStepsWithTheSameCondition() throws IOException {
        List<String> ladders = ladders();
        assertFalse(ladders.isEmpty());
        for (String ladder : ladders) {
            List<String> ten = species(read(ladder + "_10"), ladder + "_10");
            List<String> hundred = species(read(ladder + "_100"), ladder + "_100");
            assertEquals(ten, hundred, ladder + " steps differ in species");
            for (String id : SPECIES) {
                Optional<String> mapped = Ladders.ladderFor(id);
                if (mapped.isPresent() && mapped.get().equals(ladder)) {
                    assertTrue(ten.contains(id), ladder + " doesn't list " + id);
                }
            }
        }
    }

    @Test
    void everyMappedSpeciesHasALadder() throws IOException {
        List<String> ladders = ladders();
        for (String id : SPECIES) {
            Optional<String> mapped = Ladders.ladderFor(id);
            assertTrue(mapped.isPresent(), id + " has no ladder");
            assertTrue(ladders.contains(mapped.get()), mapped.get() + " has no files");
        }
    }

    @Test
    void theOldModLoadedConditionsAreGone() throws IOException {
        for (Path file : files()) {
            String text = Files.readString(file);
            assertFalse(text.contains("neoforge:mod_loaded"), file + " still has mod_loaded");
            assertFalse(text.contains("fabric:all_mods_loaded"), file + " still has all_mods_loaded");
        }
    }

    @Test
    void iconsAreVanillaOrFromTheLaddersOnlyMod() throws IOException {
        for (String ladder : ladders()) {
            List<String> species = species(read(ladder + "_10"), ladder);
            List<String> namespaces = species.stream().map(id -> id.substring(0, id.indexOf(':'))).distinct().toList();
            for (int step : Ladders.STEPS) {
                String icon = read(ladder + "_" + step).getAsJsonObject("display").getAsJsonObject("icon").get("id").getAsString();
                String namespace = icon.substring(0, icon.indexOf(':'));
                assertTrue(namespace.equals("minecraft") || namespaces.size() == 1 && namespaces.get(0).equals(namespace),
                        ladder + "_" + step + " has the icon " + icon);
            }
        }
    }

    @Test
    void everyTitleAndDescriptionHasAString() throws IOException {
        JsonObject lang = JsonParser.parseString(Files.readString(root().resolve(LANG))).getAsJsonObject();
        for (String ladder : ladders()) {
            for (int step : Ladders.STEPS) {
                JsonObject display = read(ladder + "_" + step).getAsJsonObject("display");
                for (String part : List.of("title", "description")) {
                    String key = display.getAsJsonObject(part).get("translate").getAsString();
                    assertTrue(lang.has(key), key + " is missing from en_us.json");
                }
            }
        }
    }

    // Checks both condition blocks and returns their shared species list.
    private static List<String> species(JsonObject file, String name) {
        JsonObject neoforge = file.getAsJsonArray("neoforge:conditions").get(0).getAsJsonObject();
        JsonObject fabric = file.getAsJsonArray("fabric:load_conditions").get(0).getAsJsonObject();
        assertEquals(1, file.getAsJsonArray("neoforge:conditions").size(), name);
        assertEquals(1, file.getAsJsonArray("fabric:load_conditions").size(), name);
        assertEquals("barnyard_romance:species_tracked", neoforge.get("type").getAsString(), name);
        assertEquals("barnyard_romance:species_tracked", fabric.get("condition").getAsString(), name);
        List<String> forNeoForge = strings(neoforge.getAsJsonArray("species"));
        assertEquals(forNeoForge, strings(fabric.getAsJsonArray("species")), name + " differs between loaders");
        assertFalse(forNeoForge.isEmpty(), name);
        return forNeoForge;
    }

    private static List<String> strings(JsonArray array) {
        List<String> out = new ArrayList<>();
        for (JsonElement element : array) {
            out.add(element.getAsString());
        }
        return out;
    }

    private static List<String> ladders() throws IOException {
        try (Stream<Path> each = Files.list(root().resolve(LADDER_DIR))) {
            return each.map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith("_10.json"))
                    .map(name -> name.substring(0, name.length() - "_10.json".length()))
                    .collect(java.util.stream.Collectors.toCollection(TreeSet::new)).stream().toList();
        }
    }

    private static List<Path> files() throws IOException {
        try (Stream<Path> each = Files.list(root().resolve(LADDER_DIR))) {
            return each.filter(path -> path.toString().endsWith(".json")).toList();
        }
    }

    private static JsonObject read(String name) throws IOException {
        Path file = root().resolve(LADDER_DIR).resolve(name + ".json");
        assertTrue(Files.exists(file), name + ".json doesn't exist");
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }

    private static Path root() {
        Path dir = Path.of("").toAbsolutePath();
        while (!Files.exists(dir.resolve("settings.gradle.kts"))) {
            dir = dir.getParent();
        }
        return dir;
    }
}
