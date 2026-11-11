package net.modcrafters.minecraft.barnyardromance;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Every static collection the mod fills while it runs is either cleared when the server stops (ServerScoped) or
// listed here with the reason it can outlive a world. A new one that's in neither fails here, instead of carrying
// one world's animals into the next.
class ServerScopedScanTest {
    private static final Map<String, String> OUTLIVES_THE_SERVER = Map.ofEntries(
            Map.entry("ServerScoped.CLEARS", "the clears themselves, registered once per class"),
            Map.entry("client/HerdBookHeads.STAND_INS", "client only: stand-in entities for the herd book's heads"),
            Map.entry("client/HerdBookHeads.BOXES", "client only, weak keys"),
            Map.entry("client/NestingBoxHen.COPIES", "client only, weak keys"),
            Map.entry("look/VanillaHornHider.HIDDEN", "client only: model cubes, which live as long as the models"),
            Map.entry("config/ExcludedTag.TYPES", "species id to entity type, from the built-in registry; tag contents are read fresh"),
            Map.entry("food/MinecraftItemFacts.MATCHERS", "item id or tag to matcher; tag contents are read fresh"),
            Map.entry("sex/AnimalSex.SPECIES_IDS", "entity type to species id, from the built-in registry"),
            Map.entry("food/FoodData.WARNED", "log messages already shown, cleared on reload"),
            Map.entry("look/AnimalLooks.WARNED", "log messages already shown")
    );
    // A static final field set to a new mutable collection, on one line.
    private static final Pattern FIELD = Pattern.compile(
            "static final [^=;(]+?\\s(\\w+)\\s*=\\s*(.+);");
    private static final Pattern MUTABLE = Pattern.compile(
            "new (ConcurrentHashMap|HashMap|HashSet|LinkedHashMap|LinkedHashSet|WeakHashMap|IdentityHashMap|TreeMap|TreeSet|EnumMap|ArrayList|LinkedList|ArrayDeque|CopyOnWriteArrayList|CopyOnWriteArraySet)\\b"
                    + "|newKeySet\\(|newSetFromMap\\(");

    @Test
    void everyStaticCollectionIsServerScopedOrListed() {
        Path root = SourceScan.repoRoot();
        List<String> unscoped = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        for (Path sources : mainSources(root)) {
            for (Path file : SourceScan.javaFiles(sources)) {
                String name = SourceScan.modPath(sources, file);
                List<String> lines = SourceScan.lines(file);
                // A class that clears its own state, e.g. under its own lock, registers that with ServerScoped.onStop.
                boolean clearsItself = lines.stream().anyMatch(line -> line.contains("ServerScoped.onStop("));
                for (String line : lines) {
                    Matcher field = FIELD.matcher(line);
                    if (!field.find() || !MUTABLE.matcher(field.group(2)).find()) {
                        continue;
                    }
                    String key = name + "." + field.group(1);
                    seen.add(key);
                    if (!clearsItself && !field.group(2).startsWith("ServerScoped.track(") && !OUTLIVES_THE_SERVER.containsKey(key)) {
                        unscoped.add(key);
                    }
                }
            }
        }
        assertEquals(List.of(), unscoped, "wrap these in ServerScoped.track, or list them in OUTLIVES_THE_SERVER with a reason");
        for (String listed : OUTLIVES_THE_SERVER.keySet()) {
            assertTrue(seen.contains(listed), listed + " is listed but no longer exists");
        }
    }

    private static List<Path> mainSources(Path root) {
        List<Path> dirs = new ArrayList<>(List.of(root.resolve("src/main/java"), root.resolve("fabric/src/main/java"), root.resolve("neoforge/src/main/java")));
        dirs.addAll(SourceScan.versionSources(root));
        return dirs;
    }
}
