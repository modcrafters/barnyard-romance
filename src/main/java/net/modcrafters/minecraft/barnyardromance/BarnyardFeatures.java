package net.modcrafters.minecraft.barnyardromance;

import net.modcrafters.minecraft.barnyardromance.advancement.AdvancementFeature;
import net.modcrafters.minecraft.barnyardromance.breeding.BreedingFeature;
import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.drops.DropsFeature;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsFeature;
import net.modcrafters.minecraft.barnyardromance.herd.HerdFeature;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityFeature;
import net.modcrafters.minecraft.barnyardromance.food.FoodFeature;
import net.modcrafters.minecraft.barnyardromance.milk.MilkFeature;
import net.modcrafters.minecraft.barnyardromance.rivalry.RivalryFeature;
import net.modcrafters.minecraft.barnyardromance.shear.ShearFeature;
import net.modcrafters.minecraft.barnyardromance.vigour.VigourFeature;

import java.util.List;

// Every feature, in the order they register: listeners, content and attachments run and register in this order.
// Tick hooks don't: they run in the order AnimalTicks writes down.
public final class BarnyardFeatures {
    private BarnyardFeatures() {
    }

    // Familiarity, breeding, pregnancy, eggs, feed, food, milk, shear, vigour, rivalry, drops, herd, stats, advancement,
    // naturalist, as each gets its registration.
    public static List<Feature> all() {
        return List.of(
                new FamiliarityFeature(),
                new BreedingFeature(),
                new PregnancyFeature(),
                new EggsFeature(),
                new FoodFeature(),
                new MilkFeature(),
                new ShearFeature(),
                new VigourFeature(),
                new RivalryFeature(),
                new DropsFeature(),
                // Before advancement, so a birth reaches the herd book before advancements grant on it.
                new HerdFeature(),
                new AdvancementFeature()
        );
    }

    // BarnyardRomance.init() calls this first; unit and game tests that need features call it too.
    public static synchronized void ensureInstalled() {
        if (!FeaturesRepository.installed()) {
            FeaturesRepository.install(all());
        }
    }
}
