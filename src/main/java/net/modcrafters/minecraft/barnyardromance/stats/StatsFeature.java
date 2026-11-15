package net.modcrafters.minecraft.barnyardromance.stats;

import net.modcrafters.minecraft.barnyardromance.api.BarnyardApi;
import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.stats.api.StatsApi;

public final class StatsFeature implements Feature {
    @Override
    public String name() {
        return "stats";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.provide(StatsApi.class, new StatsApiImpl());
        // The public API's queries read what the stat card reads.
        BarnyardApi.install(new AnimalQueries());
    }
}
