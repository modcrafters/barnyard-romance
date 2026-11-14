package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;
import net.modcrafters.minecraft.barnyardromance.core.registry.ItemProperties;

// Breeder's Mash and Golden Bran, the superfoods for a pregnant mother. The items are made when this class loads,
// which is while the registries are open (see register).
public final class PregnancyItems {
    public static final ResourceKey<Item> BREEDERS_MASH_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "breeders_mash")
    );
    public static final ResourceKey<Item> GOLDEN_BRAN_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "golden_bran")
    );
    public static final Item BREEDERS_MASH = new Item(ItemProperties.superfood(BREEDERS_MASH_ID));
    public static final Item GOLDEN_BRAN = new Item(ItemProperties.superfood(GOLDEN_BRAN_ID));

    private PregnancyItems() {
    }

    static void register(BarnyardRegistrations.Sink sink) {
        sink.register(Registries.ITEM, BREEDERS_MASH_ID.identifier(), () -> BREEDERS_MASH);
        sink.register(Registries.ITEM, GOLDEN_BRAN_ID.identifier(), () -> GOLDEN_BRAN);
    }
}
