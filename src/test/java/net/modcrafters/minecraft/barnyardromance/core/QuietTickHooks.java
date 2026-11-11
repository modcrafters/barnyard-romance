package net.modcrafters.minecraft.barnyardromance.core;

import java.util.ArrayList;
import java.util.List;

// A FeatureSet without a hook for every name AnimalTicks places is rejected, so tests about everything else add this
// feature: it registers a do-nothing hook under each name core doesn't register itself.
public final class QuietTickHooks {
    private static final Feature FEATURE = new Feature() {
        @Override
        public String name() {
            return "quiet tick hooks";
        }

        @Override
        public void register(FeatureRegistrar registrar) {
            for (AnimalPhase phase : AnimalPhase.values()) {
                for (String name : AnimalTicks.order(phase)) {
                    if (!AnimalTicks.coreHooks().containsKey(name)) {
                        registrar.hook(phase, name, (animal, level) -> {
                        });
                    }
                }
            }
        }
    };

    private QuietTickHooks() {
    }

    // The features, then this one.
    public static List<Feature> with(List<Feature> features) {
        List<Feature> all = new ArrayList<>(features);
        all.add(FEATURE);
        return all;
    }
}
