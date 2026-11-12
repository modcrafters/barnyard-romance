package net.modcrafters.minecraft.barnyardromance;

import net.modcrafters.minecraft.barnyardromance.advancement.AdvancementFeature;
import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureSet;
import net.modcrafters.minecraft.barnyardromance.herd.HerdFeature;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BarnyardFeaturesTest {
    // A top-level interface, public or not. A sealed interface is a closed set of values an api hands over (food's
    // Feed), not something a feature provides, so it doesn't count.
    private static final Pattern INTERFACE = Pattern.compile("^(?:public\\s+)?(?:non-sealed\\s+)?interface\\s+\\w+");

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

    // Listeners run in registration order, so herd must come before advancement.
    @Test
    void herdIsListedBeforeAdvancement() {
        List<Class<?>> order = BarnyardFeatures.all().stream().<Class<?>>map(Feature::getClass).toList();

        int herd = order.indexOf(HerdFeature.class);
        int advancement = order.indexOf(AdvancementFeature.class);

        assertTrue(herd >= 0 && herd < advancement,
                "the herd book records a birth before advancements grant on it, so HerdFeature must be listed before AdvancementFeature");
    }
}
