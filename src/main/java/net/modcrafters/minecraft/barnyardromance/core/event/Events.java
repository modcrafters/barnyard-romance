package net.modcrafters.minecraft.barnyardromance.core.event;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.FeatureSet;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;

import java.util.function.Consumer;

// Hands an event to its listeners, synchronously, in feature registration order. A listener that throws is logged and
// skipped, so the others still hear the event and whatever posted it carries on.
public final class Events {
    private Events() {
    }

    public static void post(AnimalEvent event) {
        post(FeaturesRepository.features(), event);
    }

    static void post(FeatureSet features, AnimalEvent event) {
        deliver(features, event.getClass(), event);
    }

    private static <E extends AnimalEvent> void deliver(FeatureSet features, Class<E> type, AnimalEvent event) {
        E typed = type.cast(event);
        for (Consumer<? super E> listener : features.listeners(type)) {
            try {
                listener.accept(typed);
            } catch (RuntimeException exception) {
                Constants.LOG.warn("A {} listener failed", event.getClass().getSimpleName(), exception);
            }
        }
    }
}
