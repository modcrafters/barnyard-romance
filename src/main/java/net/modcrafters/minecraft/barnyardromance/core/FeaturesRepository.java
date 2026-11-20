package net.modcrafters.minecraft.barnyardromance.core;

import net.modcrafters.minecraft.barnyardromance.core.registry.TabEntries;

import java.util.List;

// The features the game runs with: installed once at start-up (see BarnyardFeatures) and kept for the whole game.
public final class FeaturesRepository {
    private static volatile FeatureSet features;

    private FeaturesRepository() {
    }

    public static synchronized void install(List<Feature> list) {
        if (features != null) {
            throw new IllegalStateException("Features are already installed");
        }
        FeatureSet set = FeatureSet.of(list);
        // Only the game's set has every feature, so only it is checked for the tab's entries.
        TabEntries.checkRegistered(set.tabEntries().keySet());
        features = set;
    }

    public static boolean installed() {
        return features != null;
    }

    public static <T> T get(Class<T> api) {
        return features().api(api);
    }

    public static FeatureSet features() {
        FeatureSet installed = features;
        if (installed == null) {
            throw new IllegalStateException("Features aren't installed yet");
        }
        return installed;
    }

    // False before install, so unit tests that load the config without features still work.
    public static boolean trackedByDefault(String speciesId) {
        FeatureSet installed = features;
        return installed != null && installed.trackedByDefault(speciesId);
    }
}
