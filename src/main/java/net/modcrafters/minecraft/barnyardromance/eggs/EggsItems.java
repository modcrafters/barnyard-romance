package net.modcrafters.minecraft.barnyardromance.eggs;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;
import net.modcrafters.minecraft.barnyardromance.core.registry.ItemProperties;

// The fertilized egg. It is made when this class loads, which is while the registries are open (see register).
public final class EggsItems {
    public static final ResourceKey<Item> FERTILIZED_EGG_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fertilized_egg")
    );
    public static final Item FERTILIZED_EGG = new FertilizedEggItem(ItemProperties.of(FERTILIZED_EGG_ID).stacksTo(1));

    private EggsItems() {
    }

    static void register(BarnyardRegistrations.Sink sink) {
        sink.register(Registries.ITEM, FERTILIZED_EGG_ID.identifier(), () -> FERTILIZED_EGG);
    }
}
