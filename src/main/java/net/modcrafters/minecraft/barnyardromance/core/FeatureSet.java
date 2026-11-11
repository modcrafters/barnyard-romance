package net.modcrafters.minecraft.barnyardromance.core;

import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSlice;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraitCodecs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

// What a list of features registered, kept in their order. FeaturesRepository builds the game's one; tests build their own.
public final class FeatureSet {
    private final Map<Class<?>, Object> apis = new LinkedHashMap<>();
    private final Map<Class<?>, String> providers = new LinkedHashMap<>();
    private final Map<String, AnimalSlice<?>> slices = new LinkedHashMap<>();
    // Each field of the animal record to the slice that saves it.
    private final Map<String, String> sliceFields = new LinkedHashMap<>();
    private final Map<String, AttachmentKey<?>> attachments = new LinkedHashMap<>();
    // Each tick hook's name to the hook, as registered; AnimalTicks puts them in order once every feature has registered.
    private final Map<String, AnimalHook> hooks = new LinkedHashMap<>();
    private Map<AnimalPhase, Map<String, AnimalHook>> orderedHooks = Map.of();
    private final Map<Class<?>, List<Consumer<?>>> listeners = new LinkedHashMap<>();
    private final List<Consumer<BarnyardRegistrations.Sink>> content = new ArrayList<>();
    private final Map<String, Supplier<List<ItemStack>>> tabEntries = new LinkedHashMap<>();
    private final List<Predicate<String>> ignoredByDefault = new ArrayList<>();

    private FeatureSet() {
        AnimalTraitCodecs.coreFields().forEach(field -> sliceFields.put(field, "the core record"));
        hooks.putAll(AnimalTicks.coreHooks());
    }

    // Runs every feature's register in order; each feature's registrar closes when its register returns.
    public static FeatureSet of(List<Feature> features) {
        FeatureSet set = new FeatureSet();
        for (Feature feature : features) {
            Registrar registrar = set.new Registrar(feature.name());
            try {
                feature.register(registrar);
            } finally {
                registrar.closed = true;
            }
        }
        set.orderedHooks = AnimalTicks.arrange(set.hooks);
        return set;
    }

    public <T> T api(Class<T> api) {
        Object implementation = apis.get(api);
        if (implementation == null) {
            throw new IllegalStateException("No feature provides " + api.getName());
        }
        return api.cast(implementation);
    }

    public List<AnimalSlice<?>> slices() {
        return List.copyOf(slices.values());
    }

    public List<AttachmentKey<?>> attachments() {
        return List.copyOf(attachments.values());
    }

    // Each listener was registered for exactly this event class, so it accepts an E.
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E> List<Consumer<? super E>> listeners(Class<E> event) {
        return (List) Collections.unmodifiableList(listeners.getOrDefault(event, List.of()));
    }

    public List<Consumer<BarnyardRegistrations.Sink>> content() {
        return Collections.unmodifiableList(content);
    }

    public Map<String, Supplier<List<ItemStack>>> tabEntries() {
        return Collections.unmodifiableMap(tabEntries);
    }

    // The phase's hooks by name, in the order AnimalTicks runs them.
    public Map<String, AnimalHook> hooks(AnimalPhase phase) {
        return orderedHooks.get(phase);
    }

    public boolean ignoredByDefault(String speciesId) {
        return ignoredByDefault.stream().anyMatch(ignored -> ignored.test(speciesId));
    }

    private final class Registrar implements FeatureRegistrar {
        private final String feature;
        private boolean closed;

        private Registrar(String feature) {
            this.feature = feature;
        }

        @Override
        public <T> void provide(Class<T> api, T implementation) {
            open();
            String earlier = providers.putIfAbsent(api, feature);
            if (earlier != null) {
                throw new IllegalStateException(api.getName() + " is provided by both " + earlier + " and " + feature);
            }
            apis.put(api, api.cast(implementation));
        }

        // Two slices saving the same field would quietly overwrite each other in every save.
        @Override
        public void slice(AnimalSlice<?> slice) {
            open();
            if (slices.putIfAbsent(slice.name(), slice) != null) {
                throw new IllegalStateException("Two animal record slices are named " + slice.name() + "; the second is from " + feature);
            }
            for (String field : slice.fields()) {
                String earlier = sliceFields.putIfAbsent(field, slice.name());
                if (earlier != null) {
                    throw new IllegalStateException("The animal record field " + field + " is saved by both " + earlier + " and "
                            + slice.name() + " from " + feature);
                }
            }
        }

        @Override
        public void attachment(AttachmentKey<?> key) {
            open();
            if (attachments.putIfAbsent(key.id(), key) != null) {
                throw new IllegalStateException("Two attachments use the id " + key.id() + "; the second is from " + feature);
            }
        }

        @Override
        public void hook(AnimalPhase phase, String name, AnimalHook hook) {
            open();
            AnimalTicks.checkPlaced(phase, name);
            if (hooks.putIfAbsent(name, hook) != null) {
                throw new IllegalStateException("Tick hook " + name + " is registered twice; the second is from " + feature);
            }
        }

        @Override
        public <E extends AnimalEvent> void listen(Class<E> event, Consumer<? super E> listener) {
            open();
            listeners.computeIfAbsent(event, type -> new ArrayList<>()).add(listener);
        }

        @Override
        public void content(Consumer<BarnyardRegistrations.Sink> registrations) {
            open();
            content.add(registrations);
        }

        @Override
        public void tabEntry(String name, Supplier<List<ItemStack>> stacks) {
            open();
            if (tabEntries.putIfAbsent(name, stacks) != null) {
                throw new IllegalStateException("Two tab entries are named " + name + "; the second is from " + feature);
            }
        }

        @Override
        public void ignoredByDefault(Predicate<String> speciesId) {
            open();
            ignoredByDefault.add(speciesId);
        }

        private void open() {
            if (closed) {
                throw new IllegalStateException("Feature registration is closed");
            }
        }
    }
}
