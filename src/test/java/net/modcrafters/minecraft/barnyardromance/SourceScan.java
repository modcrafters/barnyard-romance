package net.modcrafters.minecraft.barnyardromance;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

// File walking for the tests that read the mod's own sources.
final class SourceScan {
    private static final String MOD_PACKAGE = "net/modcrafters/minecraft/barnyardromance/";

    private SourceScan() {
    }

    static Path repoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (!Files.exists(dir.resolve("settings.gradle.kts"))) {
            dir = dir.getParent();
        }
        return dir;
    }

    // The Stonecutter overrides, versions/<mc>/src/main/java, for each version that has any.
    static List<Path> versionSources(Path root) {
        try (Stream<Path> versions = Files.list(root.resolve("versions"))) {
            return versions.map(version -> version.resolve("src/main/java")).filter(Files::isDirectory).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static List<Path> javaFiles(Path dir) {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(file -> file.toString().endsWith(".java")).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // The file's path under the mod's package, without ".java", e.g. rivalry/Courtship.
    static String modPath(Path sources, Path file) {
        String name = sources.relativize(file).toString().replace('\\', '/');
        return name.substring(name.indexOf(MOD_PACKAGE) + MOD_PACKAGE.length(), name.length() - ".java".length());
    }

    static List<String> lines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
