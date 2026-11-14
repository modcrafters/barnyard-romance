package net.modcrafters.minecraft.barnyardromance.feed.api;

import net.minecraft.world.entity.animal.Animal;

// What other features ask of the feeding blocks.
public interface FeedApi {
    // Whether a loaded, running feeding block that holds this animal has yet to settle the days before today.
    boolean isWaitingForFeeder(Animal animal, long today);

    // Whether a running feeding block holds this animal. One that is loaded but not running doesn't count.
    boolean isHeldByRunningFeeder(Animal animal);

    // The diets, food files or tags changed, so what the feeding blocks take is worked out again.
    void foodsChanged();
}
