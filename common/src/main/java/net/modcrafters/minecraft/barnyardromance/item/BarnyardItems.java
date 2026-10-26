package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.Constants;
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
    public static final ResourceKey<Item> INSPECTOR_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "inspector")
    );
    public static final ResourceKey<Item> BREEDERS_MASH_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "breeders_mash")
    );
    public static final ResourceKey<Item> GOLDEN_BRAN_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "golden_bran")
    );
    // Vanilla keeps its tab keys private; the superfoods sit with the other food.
    public static final ResourceKey<CreativeModeTab> FOOD_AND_DRINKS = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.withDefaultNamespace("food_and_drinks")
    );
    public static final Item HERD_STICK = new HerdStick(new Item.Properties().setId(HERD_STICK_ID).stacksTo(1).rarity(Rarity.EPIC));
    public static final Item INSPECTOR = new BarnyardInspector(new Item.Properties().setId(INSPECTOR_ID).stacksTo(1).rarity(Rarity.UNCOMMON));
    // Plain items: what they do to an animal comes from their food files.
    public static final Item BREEDERS_MASH = new Item(new Item.Properties().setId(BREEDERS_MASH_ID).rarity(Rarity.UNCOMMON));
    public static final Item GOLDEN_BRAN = new Item(new Item.Properties().setId(GOLDEN_BRAN_ID).rarity(Rarity.UNCOMMON));

    private BarnyardItems() {
    }

    public static boolean isTool(ItemStack stack) {
        return stack.is(HERD_STICK) || stack.is(INSPECTOR);
    }
}
