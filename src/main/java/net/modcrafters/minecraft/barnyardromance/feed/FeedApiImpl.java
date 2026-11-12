package net.modcrafters.minecraft.barnyardromance.feed;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.feed.api.FeedApi;

final class FeedApiImpl implements FeedApi {
    @Override
    public boolean isWaitingForFeeder(Animal animal, long today) {
        return FeederIndex.waiting(animal, today);
    }

    @Override
    public boolean isHeldByRunningFeeder(Animal animal) {
        return FeederIndex.heldByRunning(animal);
    }

    @Override
    public void foodsChanged() {
        FeederFoods.clear();
    }
}
