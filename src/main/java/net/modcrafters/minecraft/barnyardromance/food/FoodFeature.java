package net.modcrafters.minecraft.barnyardromance.food;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.food.api.FoodApi;

public final class FoodFeature implements Feature {
    @Override
    public String name() {
        return "food";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.provide(FoodApi.class, new FoodApiImpl());
    }
}
