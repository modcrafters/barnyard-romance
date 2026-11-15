package net.modcrafters.minecraft.barnyardromance;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.assertTrue;

// The API jar other mods compile against holds the public API and nothing of the mod. The test task builds it first
// and passes its path.
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
}
