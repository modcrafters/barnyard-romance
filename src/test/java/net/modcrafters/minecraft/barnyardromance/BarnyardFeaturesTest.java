package net.modcrafters.minecraft.barnyardromance;

import net.modcrafters.minecraft.barnyardromance.core.FeatureSet;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BarnyardFeaturesTest {
    // A top-level interface, public or not, sealed or not.
    private static final Pattern INTERFACE = Pattern.compile("^(?:public\\s+)?(?:(?:sealed|non-sealed)\\s+)?interface\\s+\\w+");

    // Every interface in a feature's api package is provided by a feature that BarnyardFeatures lists, so a forgotten
    // registration fails here rather than in a world.
    @Test
    void everyApiHasAProvider() throws ClassNotFoundException {
        FeatureSet features = FeatureSet.of(BarnyardFeatures.all());
        Path sources = SourceScan.repoRoot().resolve("src/main/java");
        List<String> missing = new ArrayList<>();
        for (Path file : SourceScan.javaFiles(sources)) {
            String name = SourceScan.modPath(sources, file);
            if (!name.contains("/api/") || SourceScan.lines(file).stream().noneMatch(line -> INTERFACE.matcher(line).find())) {
                continue;
            }
            Class<?> api = Class.forName("net.modcrafters.minecraft.barnyardromance." + name.replace('/', '.'));
            try {
                features.api(api);
            } catch (IllegalStateException notProvided) {
                missing.add(name);
            }
        }
        assertTrue(missing.isEmpty(), () -> "no feature in BarnyardFeatures provides " + missing);
    }
}
