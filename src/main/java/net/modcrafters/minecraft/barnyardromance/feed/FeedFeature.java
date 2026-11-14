package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.feed.api.FeedApi;

public final class FeedFeature implements Feature {
    @Override
    public String name() {
        return "feed";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.provide(FeedApi.class, new FeedApiImpl());
        // A lambda, so FeedBlocks (and its blocks) load only once the registries are open.
        registrar.content(sink -> FeedBlocks.register(sink));
    }
}
