package net.modcrafters.minecraft.barnyardromance.food;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class FeedCacheTest {
    private final List<String> worked = new ArrayList<>();
    private final Function<String, Feed> work = key -> {
        worked.add(key);
        return key.endsWith("torch") ? Feed.NOT_FOOD : Feed.REFUSED;
    };

    @Test
    void anAnswerIsWorkedOutOncePerKey() {
        FeedCache<String> cache = new FeedCache<>();
        Object config = new Object();

        assertSame(Feed.NOT_FOOD, cache.get("cow/torch", config, work));
        assertSame(Feed.NOT_FOOD, cache.get("cow/torch", config, work));
        assertSame(Feed.REFUSED, cache.get("cow/meat", config, work));

        assertEquals(List.of("cow/torch", "cow/meat"), worked);
    }

    @Test
    void aNewConfigStartsAfresh() {
        FeedCache<String> cache = new FeedCache<>();
        cache.get("cow/torch", new Object(), work);
        cache.get("cow/torch", new Object(), work);

        assertEquals(List.of("cow/torch", "cow/torch"), worked);
    }

    @Test
    void clearingForgetsEveryAnswer() {
        FeedCache<String> cache = new FeedCache<>();
        Object config = new Object();
        cache.get("cow/torch", config, work);
        cache.get("cow/meat", config, work);

        cache.clear();
        cache.get("cow/torch", config, work);
        cache.get("cow/meat", config, work);

        assertEquals(List.of("cow/torch", "cow/meat", "cow/torch", "cow/meat"), worked);
    }

    // Nothing is kept for a key whose work failed, so it fails again rather than answering wrongly.
    @Test
    void aFailedAnswerIsNotKept() {
        FeedCache<String> cache = new FeedCache<>();
        Object config = new Object();
        int[] tries = {0};
        Function<String, Feed> failing = key -> {
            tries[0]++;
            throw new IllegalStateException("bad id");
        };

        for (int i = 0; i < 2; i++) {
            try {
                cache.get("cow/bad", config, failing);
            } catch (IllegalStateException expected) {
                // as it should
            }
        }

        assertEquals(2, tries[0]);
    }
}
