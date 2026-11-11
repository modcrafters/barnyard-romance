package net.modcrafters.minecraft.barnyardromance.core.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.feed.FeederKind;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.item.HerdBookCopyRecipe;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;

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
        sink.register(Registries.DATA_COMPONENT_TYPE, id("egg_data"), () -> BarnyardComponents.EGG_DATA);
        sink.register(Registries.DATA_COMPONENT_TYPE, id("wild_egg"), () -> BarnyardComponents.WILD_EGG);
        sink.register(Registries.RECIPE_SERIALIZER, HerdBookCopyRecipe.ID, () -> HerdBookCopyRecipe.SERIALIZER);
        item(sink, BarnyardItems.HERD_STICK_ID, () -> BarnyardItems.HERD_STICK);
        item(sink, BarnyardItems.HERD_BOOK_ID, () -> BarnyardItems.HERD_BOOK);
        item(sink, BarnyardItems.BREEDERS_MASH_ID, () -> BarnyardItems.BREEDERS_MASH);
        item(sink, BarnyardItems.GOLDEN_BRAN_ID, () -> BarnyardItems.GOLDEN_BRAN);
        item(sink, BarnyardItems.DAIRY_MASH_ID, () -> BarnyardItems.DAIRY_MASH);
        item(sink, BarnyardItems.STUD_MASH_ID, () -> BarnyardItems.STUD_MASH);
        item(sink, BarnyardItems.SHEEP_MILK_BUCKET_ID, () -> BarnyardItems.SHEEP_MILK_BUCKET);
        item(sink, BarnyardItems.GOAT_MILK_BUCKET_ID, () -> BarnyardItems.GOAT_MILK_BUCKET);
        item(sink, BarnyardItems.FERTILIZED_EGG_ID, () -> BarnyardItems.FERTILIZED_EGG);
        sink.register(Registries.BLOCK, BarnyardBlocks.NESTING_BOX_ID, () -> BarnyardBlocks.NESTING_BOX);
        sink.register(Registries.ITEM, BarnyardBlocks.NESTING_BOX_ID, () -> BarnyardBlocks.NESTING_BOX_ITEM);
        sink.register(Registries.BLOCK_ENTITY_TYPE, BarnyardBlocks.NESTING_BOX_ID, () -> BarnyardBlocks.NESTING_BOX_ENTITY);
        sink.register(Registries.MENU, BarnyardBlocks.NESTING_BOX_ID, () -> BarnyardBlocks.NESTING_BOX_MENU);
        for (FeederKind kind : FeederKind.values()) {
            sink.register(Registries.BLOCK, BarnyardBlocks.id(kind), () -> BarnyardBlocks.block(kind));
            sink.register(Registries.ITEM, BarnyardBlocks.id(kind), () -> BarnyardBlocks.item(kind));
        }
        // Fabric's hoppers and pipes reach the feeding blocks through the Transfer API's fallback for vanilla containers,
        // NeoForge's through the item capability (see BarnyardRomanceNeoForge.registerCapabilities).
        sink.register(Registries.BLOCK_ENTITY_TYPE, BarnyardBlocks.FEEDING_BLOCK_ID, () -> BarnyardBlocks.FEEDER_ENTITY);
        sink.register(Registries.MENU, BarnyardBlocks.FEEDING_BLOCK_ID, () -> BarnyardBlocks.FEEDER_MENU);
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
