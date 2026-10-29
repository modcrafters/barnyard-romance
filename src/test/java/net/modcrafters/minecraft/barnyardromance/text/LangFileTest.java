package net.modcrafters.minecraft.barnyardromance.text;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// Keys are always written out in full in the code, so scanning the sources finds every one.
class LangFileTest {
    private static final Pattern KEY = Pattern.compile("\"((?:gui|message|commands|item)\\.barnyard_romance\\.[a-z0-9_.]+)\"");
    // Keys the code never names in full: the game looks up item names from the registry and tag names from the tag
    // id, and BarnyardItems adds .use and .effect to a superfood's own key for its tooltip.
    // item.minecraft.milk_bucket renames vanilla's milk bucket to cow milk, next to sheep and goat milk.
    private static final Pattern IMPLICIT = Pattern.compile("item\\.barnyard_romance\\.[a-z0-9_]+(?:\\.use|\\.effect)?|item\\.minecraft\\.milk_bucket|itemGroup\\..*|death\\..*|tag\\..*");

    @Test
    void everyKeyInTheCodeIsInTheLangFile() throws IOException {
        Set<String> missing = new TreeSet<>(keysInCode());
        missing.removeAll(English.lang().keySet());
        assertEquals(Set.of(), missing, "keys used in the code but missing from en_us.json");
    }

    @Test
    void everyLangKeyIsUsed() throws IOException {
        Set<String> used = keysInCode();
        Set<String> unused = new TreeSet<>();
        for (String key : English.lang().keySet()) {
            if (!used.contains(key) && !IMPLICIT.matcher(key).matches()) {
                unused.add(key);
            }
        }
        assertEquals(Set.of(), unused, "en_us.json entries nothing uses");
    }

    @Test
    void theScanFindsTheSources() throws IOException {
        assertFalse(sourceFolders().isEmpty());
    }

    private static Set<String> keysInCode() throws IOException {
        Set<String> keys = new TreeSet<>();
        for (Path folder : sourceFolders()) {
            try (Stream<Path> files = Files.walk(folder)) {
                for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                    Matcher matcher = KEY.matcher(Files.readString(file));
                    while (matcher.find()) {
                        keys.add(matcher.group(1));
                    }
                }
            }
        }
        return keys;
    }

    // The common sources plus each version's own files, found from the project root.
    private static List<Path> sourceFolders() throws IOException {
        Path root = Path.of("").toAbsolutePath();
        while (root != null && !Files.exists(root.resolve("settings.gradle.kts"))) {
            root = root.getParent();
        }
        if (root == null) {
            throw new AssertionError("Couldn't find the project root from " + Path.of("").toAbsolutePath());
        }
        List<Path> folders = new ArrayList<>();
        for (String branch : List.of("", "fabric/", "neoforge/")) {
            Path shared = root.resolve(branch + "src/main/java");
            if (Files.isDirectory(shared)) {
                folders.add(shared);
            }
            Path versions = root.resolve(branch + "versions");
            if (Files.isDirectory(versions)) {
                try (Stream<Path> each = Files.list(versions)) {
                    each.map(version -> version.resolve("src/main/java")).filter(Files::isDirectory).forEach(folders::add);
                }
            }
        }
        return folders;
    }
}
