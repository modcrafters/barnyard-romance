package net.modcrafters.minecraft.barnyardromance;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

// A superfood's lore line is an item component from 1.20.5. On 1.20.1 ItemProperties.superfood hands the lines to its
// properties, and only a LoreItem shows them: `new Item(ItemProperties.superfood(` turns into a LoreItem there (see
// stonecutter.gradle.kts), and any other item class must extend LoreItem on 1.20.1. Anything else would build and
// quietly lose the lore, so it fails here instead, on every version.
class SuperfoodLoreScanTest {
    private static final List<String> SHARED = List.of("src/main/java", "src/gametest/java", "fabric/src", "neoforge/src",
            "forge/src");
    private static final String PROPERTIES = "core/registry/ItemProperties";
    private static final Pattern CALL = Pattern.compile("ItemProperties\\.superfood\\(");
    private static final Pattern MADE_BY = Pattern.compile("new (\\w+)\\($");
    private static final String LORE_ITEM = "extends net.modcrafters.minecraft.barnyardromance.compat.legacy.LoreItem";

    @Test
    void everySuperfoodShowsItsLoreOn1201() {
        Path root = SourceScan.repoRoot();
        Map<String, String> sources = new HashMap<>();
        for (String dir : SHARED) {
            for (Path file : SourceScan.javaFiles(root.resolve(dir))) {
                sources.put(file.getFileName().toString().replace(".java", ""), String.join("\n", SourceScan.lines(file)));
            }
        }
        List<String> found = new ArrayList<>();
        int calls = 0;
        for (String dir : SHARED) {
            for (Path file : SourceScan.javaFiles(root.resolve(dir))) {
                String name = SourceScan.modPath(root.resolve(dir), file);
                if (name.equals(PROPERTIES)) {
                    continue;
                }
                int number = 0;
                for (String line : SourceScan.lines(file)) {
                    number++;
                    if (line.contains("import static") && line.contains("ItemProperties.superfood")) {
                        found.add(name + ":" + number + "  a static import hides the call from this scan");
                    }
                    Matcher call = CALL.matcher(line);
                    while (call.find()) {
                        calls++;
                        Matcher made = MADE_BY.matcher(line.substring(0, call.start()));
                        String item = made.find() ? made.group(1) : null;
                        if (item == null) {
                            found.add(name + ":" + number + "  not the argument of `new <Item class>(`");
                        } else if (!item.equals("Item") && !sources.getOrDefault(item, "").contains(LORE_ITEM)) {
                            found.add(name + ":" + number + "  " + item + " doesn't extend LoreItem on 1.20.1");
                        }
                    }
                }
            }
        }
        assertTrue(found.isEmpty(), () -> "these superfoods would lose their lore on 1.20.1:\n" + String.join("\n", found));
        assertTrue(calls > 0, "found no superfoods: has ItemProperties.superfood moved?");
    }
}
