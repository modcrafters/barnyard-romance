package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

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
    // Plain items: what they do to an animal comes from their food files. The tooltip is the default lore,
    // so crafted stacks show it too; it is translatable, so a pack that changes the food files can reword it.
    public static final Item BREEDERS_MASH = new Item(superfood(BREEDERS_MASH_ID));
    public static final Item GOLDEN_BRAN = new Item(superfood(GOLDEN_BRAN_ID));

    private BarnyardItems() {
    }

    private static Item.Properties superfood(ResourceKey<Item> id) {
        String key = "item." + Constants.MOD_ID + "." + id.identifier().getPath();
        return new Item.Properties()
                .setId(id)
                .rarity(Rarity.UNCOMMON)
                .component(DataComponents.LORE, new ItemLore(List.of(
                        Component.translatable(key + ".use"),
                        Component.translatable(key + ".effect")
                )));
    }

        public static boolean isTool(ItemStack stack) {
        return stack.is(HERD_STICK) || stack.is(INSPECTOR);
    }
}
