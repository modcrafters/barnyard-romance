package net.modcrafters.minecraft.barnyardromance;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

// The sources every version shares must build on Java 17, for Minecraft 1.20.1: the Java 21 list, clamp and
// StringBuilder methods go through Jdk, mod components through ItemData, and a switch matches a type with if and
// instanceof. The per-version copies under versions/ compile on their own Java and aren't scanned, and neither are
// comments, which hold the other side of a version comment.
class Java17ScanTest {
    private static final List<String> SHARED = List.of("src/main/java", "src/api/java", "src/gametest/java",
            "fabric/src", "neoforge/src", "forge/src");
    private static final String JDK = "core/compat/Jdk";
    private static final String REVERSED = "List.reversed, use Jdk.reversed";
    private static final Map<String, Pattern> BANNED = Map.of(
            "Math.clamp, use Jdk.clamp", Pattern.compile("\\bMath\\.clamp\\("),
            "a Java 21 list method, use Jdk.first, last, removeFirst or removeLast",
            Pattern.compile("\\.(getFirst|getLast|removeFirst|removeLast)\\(\\)"),
            REVERSED, Pattern.compile("\\.reversed\\(\\)"),
            "StringBuilder.repeat, use Jdk.repeat", Pattern.compile("(?i)(builder|\\bsb)\\w*\\.repeat\\("),
            "a pattern-matching switch case, use if and instanceof", Pattern.compile("\\bcase [A-Z][A-Za-z.]+ [a-z]\\w* *(->|:|when\\b)"),
            "a mod component on an ItemStack, use ItemData",
            Pattern.compile("\\.(get|set|has|remove|update|getOrDefault)\\(\\s*(?!Data)\\w*Components\\.[A-Z_]+"));
    // Comparator.reversed has been there since Java 8.
    private static final Pattern COMPARATOR = Pattern.compile("Comparator\\.|\\.thenComparing");
    // Lines that look like a banned call but build on Java 17, as "<file under the mod's package>: <the line>". An
    // entry that no longer matches a line fails, so this only shrinks.
    private static final Map<String, String> ALLOWED = Map.of(
            "api/SizeClass: t = Math.clamp(t, 0.0, 1.0);",
            "the API jar can't use Jdk; this line sits beside a version comment holding the Java 17 form",
            "core/compat/MoobloomSwap: for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {",
            "ASM's InsnList, not a java.util.List"
    );

    @Test
    void sharedSourcesBuildOnJava17() {
        Path root = SourceScan.repoRoot();
        List<String> found = new ArrayList<>();
        Set<String> allowedSeen = new TreeSet<>();
        for (String dir : SHARED) {
            for (Path file : SourceScan.javaFiles(root.resolve(dir))) {
                String name = SourceScan.modPath(root, file);
                if (name.equals(JDK)) {
                    continue;
                }
                boolean inBlockComment = false;
                int number = 0;
                for (String line : SourceScan.lines(file)) {
                    number++;
                    String stripped = line.strip();
                    boolean comment = inBlockComment || stripped.startsWith("//") || stripped.startsWith("/*") || stripped.startsWith("*");
                    if (stripped.contains("/*") && !stripped.contains("*/")) {
                        inBlockComment = true;
                    }
                    if (stripped.contains("*/")) {
                        inBlockComment = false;
                    }
                    if (comment) {
                        continue;
                    }
                    String code = stripped.contains("//") ? stripped.substring(0, stripped.indexOf("//")) : stripped;
                    for (Map.Entry<String, Pattern> banned : BANNED.entrySet()) {
                        if (!banned.getValue().matcher(code).find()
                                || banned.getKey().equals(REVERSED) && COMPARATOR.matcher(code).find()) {
                            continue;
                        }
                        String key = name + ": " + stripped;
                        if (ALLOWED.containsKey(key)) {
                            allowedSeen.add(key);
                        } else {
                            found.add(name + ":" + number + "  " + banned.getKey() + "\n        " + stripped);
                        }
                    }
                }
            }
        }
        assertTrue(found.isEmpty(), () -> found.size() + " sites break the Java 17 build:\n" + String.join("\n", found));
        List<String> stale = ALLOWED.keySet().stream().filter(key -> !allowedSeen.contains(key)).sorted().toList();
        assertTrue(stale.isEmpty(), () -> "these ALLOWED lines are gone or changed:\n" + String.join("\n", stale));
    }

    @Test
    void theScannedFoldersExist() {
        Path root = SourceScan.repoRoot();
        assertTrue(Files.isDirectory(root.resolve("src/main/java")) && Files.isDirectory(root.resolve("src/gametest/java"))
                && Files.isDirectory(root.resolve("fabric/src")) && Files.isDirectory(root.resolve("neoforge/src"))
                && Files.isDirectory(root.resolve("forge/src")));
    }
}
