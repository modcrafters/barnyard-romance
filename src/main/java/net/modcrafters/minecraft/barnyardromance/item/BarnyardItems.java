package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.registry.ItemProperties;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public final class BarnyardItems {
    public static final ResourceKey<Item> HERD_STICK_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_stick")
    );
    public static final ResourceKey<Item> BREEDERS_MASH_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "breeders_mash")
    );
    public static final ResourceKey<Item> GOLDEN_BRAN_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "golden_bran")
    );
    public static final ResourceKey<Item> HERD_BOOK_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book")
    );
    public static final ResourceKey<Item> FERTILIZED_EGG_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fertilized_egg")
    );
    // Vanilla keeps its tab keys private; the superfoods sit with the other food.
    public static final ResourceKey<CreativeModeTab> FOOD_AND_DRINKS = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.withDefaultNamespace("food_and_drinks")
    );
    public static final Item HERD_STICK = new HerdStick(ItemProperties.of(HERD_STICK_ID).stacksTo(1).rarity(Rarity.EPIC));
    public static final Item HERD_BOOK = new HerdBookItem(ItemProperties.of(HERD_BOOK_ID).stacksTo(1));
    public static final Item FERTILIZED_EGG = new FertilizedEggItem(ItemProperties.of(FERTILIZED_EGG_ID).stacksTo(1));
    public static final Item BREEDERS_MASH = new Item(ItemProperties.superfood(BREEDERS_MASH_ID));
    public static final Item GOLDEN_BRAN = new Item(ItemProperties.superfood(GOLDEN_BRAN_ID));

    private BarnyardItems() {
    }

    // Our items that act on an animal before it does, so a horse doesn't rear and a tamed cat doesn't sit instead.
    public static boolean isTool(ItemStack stack) {
        return stack.is(HERD_STICK) || stack.is(HERD_BOOK);
    }
}
