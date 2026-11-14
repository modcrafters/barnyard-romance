package net.modcrafters.minecraft.barnyardromance.food;

import net.modcrafters.minecraft.barnyardromance.food.api.Feed;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

// Remembered answers to "what does this species make of this item". They hold for as long as the config, the diets,
// the food files and the tags they were worked out from: a different config object starts afresh by itself, and the
// rest call clear when they reload.
public final class FeedCache<K> {
    private final Map<K, Feed> answers = new ConcurrentHashMap<>();
    private volatile Object source;

    public Feed get(K key, Object source, Function<K, Feed> work) {
        if (this.source != source) {
            answers.clear();
            this.source = source;
        }
        return answers.computeIfAbsent(key, work);
    }

    public void clear() {
        answers.clear();
    }
}
