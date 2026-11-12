package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;
import net.modcrafters.minecraft.barnyardromance.core.registry.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

// The herd book and the herd stick. The items are made when this class loads, which is while the registries are open
// (see register).
public final class HerdItems {
    public static final ResourceKey<Item> HERD_STICK_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_stick")
    );
    public static final ResourceKey<Item> HERD_BOOK_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book")
    );
    public static final Item HERD_STICK = new HerdStick(ItemProperties.of(HERD_STICK_ID).stacksTo(1).rarity(Rarity.EPIC));
    public static final Item HERD_BOOK = new HerdBookItem(ItemProperties.of(HERD_BOOK_ID).stacksTo(1));

    private HerdItems() {
    }

    static void register(BarnyardRegistrations.Sink sink) {
        sink.register(Registries.ITEM, HERD_STICK_ID.identifier(), () -> HERD_STICK);
        sink.register(Registries.ITEM, HERD_BOOK_ID.identifier(), () -> HERD_BOOK);
    }

    // Our items that act on an animal before it does, so a horse doesn't rear and a tamed cat doesn't sit instead.
    public static boolean isTool(ItemStack stack) {
        return stack.is(HERD_STICK) || stack.is(HERD_BOOK);
    }
}
