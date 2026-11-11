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

// Pins the creative tab's order. Filling the tab makes the mod's items, which needs the game's registries open, so
// this reads the order BarnyardTab.contents declares instead: each stack it adds, in order, with the if or for it
// sits in.
class BarnyardTabTest {
    private static final String SOURCE = "src/main/java/net/modcrafters/minecraft/barnyardromance/core/registry/BarnyardTab.java";
    private static final Pattern ADD = Pattern.compile("^contents\\.add\\((.*)\\);$");

    @Test
    void tabOrderIsUnchanged() throws IOException {
        assertEquals(List.of(
                "new ItemStack(BarnyardItems.HERD_BOOK)",
                "new ItemStack(BarnyardBlocks.NESTING_BOX_ITEM)",
                "for (FeederKind kind : FeederKind.values()) | new ItemStack(BarnyardBlocks.item(kind))",
                "if (herdSticks) | HerdStick.create(HerdTool.info())",
                "if (herdSticks) | HerdStick.create(HerdTool.familiarity(1))",
                "if (herdSticks) | HerdStick.create(HerdTool.familiarity(-1))",
                "if (herdSticks) | HerdStick.create(HerdTool.pregnancy(1))",
                "if (herdSticks) | HerdStick.create(HerdTool.pregnancy(-1))",
                "new ItemStack(BarnyardItems.BREEDERS_MASH)",
                "new ItemStack(BarnyardItems.GOLDEN_BRAN)",
                "new ItemStack(item(\"dairy_mash\"))",
                "new ItemStack(item(\"stud_mash\"))",
                "new ItemStack(item(\"sheep_milk_bucket\"))",
                "new ItemStack(item(\"goat_milk_bucket\"))"
        ), declaredContents());
        // The feeders' place in the tab is FeederKind's order.
        assertEquals(List.of("bird_feeder", "trough", "feeder", "manger", "pet_bowl"),
                Arrays.stream(FeederKind.values()).map(FeederKind::id).toList());
    }

    private static List<String> declaredContents() throws IOException {
        List<String> lines = Files.readAllLines(repoRoot().resolve(SOURCE)).stream().map(String::trim).toList();
        int start = lines.indexOf("public static List<ItemStack> contents(boolean herdSticks) {");
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
                Matcher add = ADD.matcher(line);
                if (add.find()) {
                    List<String> around = new ArrayList<>(blocks.reversed());
                    around.add(add.group(1));
                    adds.add(String.join(" | ", around));
                }
            }
        }
        return adds;
    }

    private static Path repoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (!Files.exists(dir.resolve("settings.gradle.kts"))) {
            dir = dir.getParent();
        }
        return dir;
    }
}
