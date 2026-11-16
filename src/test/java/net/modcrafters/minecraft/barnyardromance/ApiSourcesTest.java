package net.modcrafters.minecraft.barnyardromance;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

// The public API is compiled into other mods, so it may only use Minecraft, the JDK and itself. Gradle already keeps
// it from seeing the mod's main code; this keeps it off the libraries main has on its classpath too.
class ApiSourcesTest {
    private static final Pattern IMPORT = Pattern.compile("^\\s*import\\s+(?:static\\s+)?([\\w.]+)");
    private static final List<String> ALLOWED = List.of(
            "java.",
            "net.minecraft.",
            "com.mojang.",
            "org.jspecify.",
            "org.slf4j.",
            "net.modcrafters.minecraft.barnyardromance.api."
    );

    @Test
    void apiFolderExists() {
        Path sex = SourceScan.repoRoot().resolve("src/api/java/net/modcrafters/minecraft/barnyardromance/api/Sex.java");
        assertTrue(Files.exists(sex), () -> "no public API at " + sex);
    }

    @Test
    void apiImportsOnlyMinecraftAndItself() {
        Path root = SourceScan.repoRoot();
        List<Path> dirs = new ArrayList<>(List.of(root.resolve("src/api/java")));
        dirs.addAll(versionApiSources(root));
        List<String> broken = brokenImports(dirs, ALLOWED);
        assertTrue(broken.isEmpty(), () -> "the public API may only import Minecraft, the JDK and itself:\n" + String.join("\n", broken));
    }

    // A loader's native layer may use its loader too, and nothing of the mod outside the API.
    @Test
    void loaderApiImportsOnlyItsLoaderMinecraftAndTheApi() {
        Path root = SourceScan.repoRoot();
        Map<String, String> loaders = Map.of("neoforge", "net.neoforged.", "fabric", "net.fabricmc.");
        List<String> broken = new ArrayList<>();
        loaders.forEach((loader, loaderPackage) -> {
            Path dir = root.resolve(loader + "/src/api/java");
            assertTrue(!SourceScan.javaFiles(dir).isEmpty(), () -> "no " + loader + " API sources at " + dir);
            List<String> allowed = new ArrayList<>(ALLOWED);
            allowed.add(loaderPackage);
            broken.addAll(brokenImports(List.of(dir), allowed).stream().map(line -> loader + ": " + line).toList());
        });
        assertTrue(broken.isEmpty(), () -> "a loader's API may only import its loader, Minecraft, the JDK and the API:\n" + String.join("\n", broken));
    }

    private static List<String> brokenImports(List<Path> dirs, List<String> allowed) {
        List<String> broken = new ArrayList<>();
        for (Path dir : dirs) {
            for (Path file : SourceScan.javaFiles(dir)) {
                for (String line : SourceScan.lines(file)) {
                    Matcher matcher = IMPORT.matcher(line);
                    if (matcher.find() && allowed.stream().noneMatch(matcher.group(1)::startsWith)) {
                        broken.add(dir.relativize(file) + " -> " + matcher.group(1));
                    }
                }
            }
        }
        return broken;
    }

    // The javadoc jar and an IDE's quick doc only show /** */ comments, so every public type in the API (the common
    // one and the loaders' native layers) has one.
    @Test
    void everyPublicApiTypeHasJavadoc() {
        Path root = SourceScan.repoRoot();
        List<Path> dirs = new ArrayList<>(List.of(root.resolve("src/api/java"), root.resolve("neoforge/src/api/java"), root.resolve("fabric/src/api/java")));
        dirs.addAll(versionApiSources(root));
        List<String> bare = new ArrayList<>();
        for (Path dir : dirs) {
            for (Path file : SourceScan.javaFiles(dir)) {
                List<String> lines = SourceScan.lines(file);
                for (int i = 0; i < lines.size(); i++) {
                    if (TYPE.matcher(lines.get(i)).find() && !documented(lines, i)) {
                        bare.add(dir.relativize(file) + ":" + (i + 1));
                    }
                }
            }
        }
        assertTrue(bare.isEmpty(), () -> "public API types without javadoc:\n" + String.join("\n", bare));
    }

    private static final Pattern TYPE = Pattern.compile("^\\s*public\\s+(?:static\\s+|final\\s+|abstract\\s+)*(?:class|interface|enum|record)\\s");

    // The nearest line above the declaration, past annotations, closes a /** */ comment.
    private static boolean documented(List<String> lines, int declaration) {
        for (int i = declaration - 1; i >= 0; i--) {
            String line = lines.get(i).strip();
            if (line.startsWith("@")) {
                continue;
            }
            if (!line.endsWith("*/")) {
                return false;
            }
            for (int j = i; j >= 0; j--) {
                if (lines.get(j).strip().startsWith("/**")) {
                    return true;
                }
                if (lines.get(j).strip().startsWith("/*")) {
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    private static List<Path> versionApiSources(Path root) {
        try (Stream<Path> versions = Files.list(root.resolve("versions"))) {
            return versions.map(version -> version.resolve("src/api/java")).filter(Files::isDirectory).toList();
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }
}
