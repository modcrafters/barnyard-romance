package net.modcrafters.minecraft.barnyardromance.core.text;

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
    private static final Pattern KEY = Pattern.compile("\"((?:gui|message|commands|item|container)\\.barnyard_romance\\.[a-z0-9_.]+)\"");
    // Advancement titles and descriptions are named in the advancement files, not the code.
    private static final Pattern ADVANCEMENT_KEY = Pattern.compile("\"(advancements\\.barnyard_romance\\.[a-z0-9_.]+)\"");
    private static final String ADVANCEMENTS = "src/main/resources/data/barnyard_romance/advancement";
    // Keys the code never names in full: the game looks up item and block names from the registry and tag names from the tag
    // id, and BarnyardItems adds .use and .effect to a superfood's own key for its tooltip.
    // config.jade.plugin_<id>.<option> is the name Jade shows for a plugin option in its own settings screen.
    // item.minecraft.milk_bucket renames vanilla's milk bucket to cow milk, next to sheep and goat milk.
    private static final Pattern IMPLICIT = Pattern.compile("item\\.barnyard_romance\\.[a-z0-9_]+(?:\\.use|\\.effect)?|block\\.barnyard_romance\\.[a-z0-9_]+|item\\.minecraft\\.milk_bucket|itemGroup\\..*|death\\..*|tag\\..*|config\\.jade\\.plugin_barnyard_romance\\.[a-z_]+");

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
        assertFalse(sourceFolders("src/main/java").isEmpty());
        assertFalse(sourceFolders(ADVANCEMENTS).isEmpty());
    }

    private static Set<String> keysInCode() throws IOException {
        Set<String> keys = new TreeSet<>();
        scan(sourceFolders("src/main/java"), ".java", KEY, keys);
        scan(sourceFolders(ADVANCEMENTS), ".json", ADVANCEMENT_KEY, keys);
        return keys;
    }

    private static void scan(List<Path> folders, String extension, Pattern pattern, Set<String> keys) throws IOException {
        for (Path folder : folders) {
            try (Stream<Path> files = Files.walk(folder)) {
                for (Path file : files.filter(path -> path.toString().endsWith(extension)).toList()) {
                    Matcher matcher = pattern.matcher(Files.readString(file));
                    while (matcher.find()) {
                        keys.add(matcher.group(1));
                    }
                }
            }
        }
    }

    // The common folder plus each version's own, found from the project root; sub is a path under a source root.
    private static List<Path> sourceFolders(String sub) throws IOException {
        Path root = Path.of("").toAbsolutePath();
        while (root != null && !Files.exists(root.resolve("settings.gradle.kts"))) {
            root = root.getParent();
        }
        if (root == null) {
            throw new AssertionError("Couldn't find the project root from " + Path.of("").toAbsolutePath());
        }
        List<Path> folders = new ArrayList<>();
        for (String branch : List.of("", "fabric/", "neoforge/")) {
            Path shared = root.resolve(branch + sub);
            if (Files.isDirectory(shared)) {
                folders.add(shared);
            }
            Path versions = root.resolve(branch + "versions");
            if (Files.isDirectory(versions)) {
                try (Stream<Path> each = Files.list(versions)) {
                    each.map(version -> version.resolve(sub)).filter(Files::isDirectory).forEach(folders::add);
                }
            }
        }
        return folders;
    }
}
