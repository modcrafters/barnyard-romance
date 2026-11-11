package net.modcrafters.minecraft.barnyardromance.core;

import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.core.data.AttachmentKey;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

// What a feature hands over while it registers. Registration closes once every feature has registered; a call after
// that throws.
public interface FeatureRegistrar {
    // The interface other features call this one through, and its implementation. One provider per interface.
    <T> void provide(Class<T> api, T implementation);

    // Data the feature keeps on an animal, saved and synced to clients. Each id is used once.
    void attachment(AttachmentKey<?> key);

    // Runs when the event is posted, after the listeners of features registered earlier.
    <E extends AnimalEvent> void listen(Class<E> event, Consumer<? super E> listener);

    // The feature's blocks, items, components and the like, put into Minecraft's registries at start-up.
    void content(Consumer<BarnyardRegistrations.Sink> content);

    // Stacks the creative tab shows under a name its order list uses (see BarnyardTab).
    void tabEntry(String name, Supplier<List<ItemStack>> stacks);

    // Species the mod leaves alone unless the config says otherwise.
    void ignoredByDefault(Predicate<String> speciesId);
}
