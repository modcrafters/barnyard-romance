package net.modcrafters.minecraft.barnyardromance;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsFeature;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;

import java.util.List;

// Every feature, in the order they register: listeners, content and attachments run and register in this order.
public final class BarnyardFeatures {
    private BarnyardFeatures() {
    }

    // Familiarity, breeding, pregnancy, eggs, feed, food, milk, shear, vigour, rivalry, drops, herd, stats, advancement,
    // naturalist, as each gets its registration.
    public static List<Feature> all() {
        return List.of(
                new PregnancyFeature(),
                new EggsFeature()
        );
    }

    // BarnyardRomance.init() calls this first; unit and game tests that need features call it too.
    public static synchronized void ensureInstalled() {
        if (!FeaturesRepository.installed()) {
            FeaturesRepository.install(all());
        }
    }
}
