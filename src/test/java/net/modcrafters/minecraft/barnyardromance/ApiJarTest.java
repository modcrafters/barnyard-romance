package net.modcrafters.minecraft.barnyardromance;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.assertTrue;

// The API jars other mods compile against hold the public API and nothing of the mod: the common one only the common
// API, each loader's the common API and that loader's native events. A loader's mod sources jar carries its API too.
// The test task builds the jars first and passes their paths.
class ApiJarTest {
    private static final String API = "net/modcrafters/minecraft/barnyardromance/api/";

    @Test
    void apiJarHoldsOnlyTheApi() throws IOException {
        String property = System.getProperty("barnyard_romance.api_jar");
        assertTrue(property != null, "the test task doesn't pass the API jar's path");
        Path jar = Path.of(property);
        assertTrue(Files.exists(jar), () -> "no API jar at " + jar);
        try (JarFile file = new JarFile(jar.toFile())) {
            List<String> classes = file.stream().map(ZipEntry::getName).filter(name -> name.endsWith(".class")).toList();
            List<String> strays = classes.stream().filter(name -> !name.startsWith(API)).toList();

            assertTrue(strays.isEmpty(), () -> "the API jar holds " + strays);
            assertTrue(classes.contains(API + "Sex.class"), "the API jar has no Sex");
            assertTrue(classes.contains(API + "BarnyardEvents.class"), "the API jar has no BarnyardEvents");
        }
    }

    // A modder adds only the loader's API jar, so it holds the whole API: the common API and that loader's native
    // events, and nothing of the mod or of another loader.
    @Test
    void loaderApiJarsHoldTheWholeApi() throws IOException {
        for (LoaderJars loader : loaderJars()) {
            String own = API + loader.name() + "/";
            String other = API + (loader.name().equals("fabric") ? "neoforge" : "fabric") + "/";
            List<String> classes = entries(loader.api()).stream().filter(name -> name.endsWith(".class")).toList();
            List<String> strays = classes.stream().filter(name -> !name.startsWith(API) || name.startsWith(other)).toList();

            assertTrue(strays.isEmpty(), () -> loader.name() + "'s API jar holds " + strays);
            assertTrue(classes.contains(API + "Sex.class"), () -> loader.name() + "'s API jar has no Sex");
            assertTrue(classes.contains(API + "BarnyardEvents.class"), () -> loader.name() + "'s API jar has no BarnyardEvents");
            assertTrue(classes.contains(own + loader.knownType() + ".class"), () -> loader.name() + "'s API jar has no " + loader.knownType());
        }
    }

    // So a modder's IDE shows the native events' source, not decompiled code.
    @Test
    void loaderSourcesJarsHoldTheirApi() throws IOException {
        for (LoaderJars loader : loaderJars()) {
            List<String> sources = entries(loader.sources());

            assertTrue(sources.contains(API + "Sex.java"), () -> loader.name() + "'s sources jar has no common API");
            String own = API + loader.name() + "/" + loader.knownType() + ".java";
            assertTrue(sources.contains(own), () -> loader.name() + "'s sources jar has no " + own);
        }
    }

    private record LoaderJars(String name, Path api, Path sources) {
        // A type each loader's native layer has.
        String knownType() {
            return name.equals("fabric") ? "BarnyardFabricEvents" : "MilkingEvent";
        }
    }

    private static List<LoaderJars> loaderJars() {
        String property = System.getProperty("barnyard_romance.loader_jars");
        assertTrue(property != null && !property.isBlank(), "the test task doesn't pass any loader's jars");
        return java.util.Arrays.stream(property.split(";")).map(entry -> {
            String[] parts = entry.split(java.util.regex.Pattern.quote("|"));
            return new LoaderJars(parts[0], Path.of(parts[1]), Path.of(parts[2]));
        }).toList();
    }

    private static List<String> entries(Path jar) throws IOException {
        assertTrue(Files.exists(jar), () -> "no jar at " + jar);
        try (JarFile file = new JarFile(jar.toFile())) {
            return file.stream().map(ZipEntry::getName).toList();
        }
    }
}
