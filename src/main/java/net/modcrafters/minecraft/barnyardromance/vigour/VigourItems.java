package net.modcrafters.minecraft.barnyardromance.vigour;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;
import net.modcrafters.minecraft.barnyardromance.core.registry.ItemProperties;

// Stud Mash. The item is made when this class loads, which is while the registries are open (see register).
public final class VigourItems {
    public static final ResourceKey<Item> STUD_MASH_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "stud_mash")
    );
    public static final Item STUD_MASH = new Item(ItemProperties.superfood(STUD_MASH_ID));

    private VigourItems() {
    }

    static void register(BarnyardRegistrations.Sink sink) {
        sink.register(Registries.ITEM, STUD_MASH_ID.identifier(), () -> STUD_MASH);
    }
}
