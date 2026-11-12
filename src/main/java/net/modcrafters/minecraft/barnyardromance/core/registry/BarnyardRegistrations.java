package net.modcrafters.minecraft.barnyardromance.core.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.item.HerdBookCopyRecipe;

import java.util.function.Consumer;
import java.util.function.Supplier;

// Everything the mod adds to Minecraft's registries, for both loaders: Fabric walks it once at start-up, NeoForge
// from each RegisterEvent, keeping the entries for the registry being filled. The values are suppliers, so nothing
// is built before its registry is open. Each feature's own content follows, in feature registration order.
public final class BarnyardRegistrations {
    // Where each loader puts an entry.
    public interface Sink {
        <T> void register(ResourceKey<? extends Registry<T>> registry, Identifier id, Supplier<? extends T> value);
    }

    private BarnyardRegistrations() {
    }

    // The tab comes from the loader, which has the builder and can fill it (see BarnyardTab).
    public static void registerAll(Sink sink, Supplier<CreativeModeTab> tab) {
        sink.register(Registries.DATA_COMPONENT_TYPE, id("herd_tool"), () -> BarnyardComponents.HERD_TOOL);
        sink.register(Registries.DATA_COMPONENT_TYPE, id("herd_book"), () -> BarnyardComponents.HERD_BOOK);
        sink.register(Registries.RECIPE_SERIALIZER, HerdBookCopyRecipe.ID, () -> HerdBookCopyRecipe.SERIALIZER);
        item(sink, BarnyardItems.HERD_STICK_ID, () -> BarnyardItems.HERD_STICK);
        item(sink, BarnyardItems.HERD_BOOK_ID, () -> BarnyardItems.HERD_BOOK);
        sink.register(Registries.CREATIVE_MODE_TAB, BarnyardTab.ID.identifier(), tab);
        sink.register(Registries.PARTICLE_TYPE, id("pregnancy_heart"), () -> BarnyardParticles.PREGNANCY_HEART);
        sink.register(Registries.PARTICLE_TYPE, id("old_heart"), () -> BarnyardParticles.OLD_HEART);
        for (Consumer<Sink> content : FeaturesRepository.features().content()) {
            content.accept(sink);
        }
    }

    private static void item(Sink sink, ResourceKey<Item> key, Supplier<Item> item) {
        sink.register(Registries.ITEM, key.identifier(), item);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
