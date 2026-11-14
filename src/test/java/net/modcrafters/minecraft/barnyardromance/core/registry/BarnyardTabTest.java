package net.modcrafters.minecraft.barnyardromance.core.registry;

import net.modcrafters.minecraft.barnyardromance.feed.FeederKind;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pins the creative tab's order. Filling the tab makes the mod's items, which needs the game's registries open, so
// this reads the order BarnyardTab.contents declares instead: each stack it adds, in order, with the if or for it
// sits in. The herd sticks are a tab entry herd registers, so their order and their config gate are read from there.
class BarnyardTabTest {
    private static final String TAB = "src/main/java/net/modcrafters/minecraft/barnyardromance/core/registry/BarnyardTab.java";
    private static final String STICK = "src/main/java/net/modcrafters/minecraft/barnyardromance/herd/HerdStick.java";
    private static final String HERD = "src/main/java/net/modcrafters/minecraft/barnyardromance/herd/HerdFeature.java";

    @Test
    void tabOrderIsUnchanged() throws IOException {
        assertEquals(List.of(
                "new ItemStack(item(\"herd_book\"))",
                "new ItemStack(item(\"nesting_box\"))",
                "new ItemStack(item(\"bird_feeder\"))",
                "new ItemStack(item(\"trough\"))",
                "new ItemStack(item(\"feeder\"))",
                "new ItemStack(item(\"manger\"))",
                "new ItemStack(item(\"pet_bowl\"))",
                "entry(\"herd_sticks\")",
                "new ItemStack(item(\"breeders_mash\"))",
                "new ItemStack(item(\"golden_bran\"))",
                "new ItemStack(item(\"dairy_mash\"))",
                "new ItemStack(item(\"stud_mash\"))",
                "new ItemStack(item(\"sheep_milk_bucket\"))",
                "new ItemStack(item(\"goat_milk_bucket\"))"
        ), declared(TAB, "public static List<ItemStack> contents() {", "contents"));
        assertEquals(List.of(
                "if (herdSticks) | create(HerdTool.info())",
                "if (herdSticks) | create(HerdTool.familiarity(1))",
                "if (herdSticks) | create(HerdTool.familiarity(-1))",
                "if (herdSticks) | create(HerdTool.pregnancy(1))",
                "if (herdSticks) | create(HerdTool.pregnancy(-1))"
        ), declared(STICK, "public static List<ItemStack> tabStacks(boolean herdSticks) {", "sticks"));
        // herd_sticks is the sticks, with the config read when the tab is filled.
        assertTrue(lines(HERD).contains("registrar.tabEntry(\"herd_sticks\", () -> HerdStick.tabStacks(BarnyardConfig.current().herdSticks()));"),
                "HerdFeature no longer registers herd_sticks as the sticks the loaded config allows");
        // The tab names the feeders in FeederKind's order, as when it went through FeederKind.values().
        assertEquals(List.of("bird_feeder", "trough", "feeder", "manger", "pet_bowl"),
                Arrays.stream(FeederKind.values()).map(FeederKind::id).toList());
    }

    // Each add or addAll to the list in the method, in order, with the blocks around it.
    private static List<String> declared(String source, String method, String list) throws IOException {
        Pattern add = Pattern.compile("^" + Pattern.quote(list) + "\\.add(?:All)?\\((.*)\\);$");
        List<String> lines = lines(source);
        int start = lines.indexOf(method);
        assertTrue(start >= 0, () -> source + " has no " + method);
        List<String> adds = new ArrayList<>();
        Deque<String> blocks = new ArrayDeque<>();
        for (String line : lines.subList(start + 1, lines.size())) {
            if (line.equals("}")) {
                if (blocks.isEmpty()) {
                    break;
                }
                blocks.pop();
            } else if (line.endsWith("{")) {
                blocks.push(line.substring(0, line.length() - 1).trim());
            } else {
                Matcher matcher = add.matcher(line);
                if (matcher.find()) {
                    List<String> around = new ArrayList<>(blocks.reversed());
                    around.add(matcher.group(1));
                    adds.add(String.join(" | ", around));
                }
            }
        }
        return adds;
    }

    private static List<String> lines(String source) throws IOException {
        return Files.readAllLines(repoRoot().resolve(source)).stream().map(String::trim).toList();
    }

    private static Path repoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (!Files.exists(dir.resolve("settings.gradle.kts"))) {
            dir = dir.getParent();
        }
        return dir;
    }
}
