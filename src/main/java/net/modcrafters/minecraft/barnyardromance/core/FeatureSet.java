package net.modcrafters.minecraft.barnyardromance.core;

import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;

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
    private final Map<String, AttachmentKey<?>> attachments = new LinkedHashMap<>();
    private final Map<Class<?>, List<Consumer<?>>> listeners = new LinkedHashMap<>();
    private final List<Consumer<BarnyardRegistrations.Sink>> content = new ArrayList<>();
    private final Map<String, Supplier<List<ItemStack>>> tabEntries = new LinkedHashMap<>();
    private final List<Predicate<String>> ignoredByDefault = new ArrayList<>();

    private FeatureSet() {
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
        return set;
    }

    public <T> T api(Class<T> api) {
        Object implementation = apis.get(api);
        if (implementation == null) {
            throw new IllegalStateException("No feature provides " + api.getName());
        }
        return api.cast(implementation);
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

        @Override
        public void attachment(AttachmentKey<?> key) {
            open();
            if (attachments.putIfAbsent(key.id(), key) != null) {
                throw new IllegalStateException("Two attachments use the id " + key.id() + "; the second is from " + feature);
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
