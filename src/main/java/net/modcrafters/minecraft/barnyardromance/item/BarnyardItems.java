package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
//? if >=1.21.2 {
import net.minecraft.world.item.component.Consumables;
//?} else
//import net.minecraft.world.item.MilkBucketItem;

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
    public static final ResourceKey<Item> DAIRY_MASH_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "dairy_mash")
    );
    public static final ResourceKey<Item> SHEEP_MILK_BUCKET_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "sheep_milk_bucket")
    );
    public static final ResourceKey<Item> GOAT_MILK_BUCKET_ID = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "goat_milk_bucket")
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
    public static final Item HERD_STICK = new HerdStick(properties(HERD_STICK_ID).stacksTo(1).rarity(Rarity.EPIC));
    public static final Item INSPECTOR = new BarnyardInspector(properties(INSPECTOR_ID).stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final Item HERD_BOOK = new HerdBookItem(properties(HERD_BOOK_ID).stacksTo(1));
    public static final Item FERTILIZED_EGG = new FertilizedEggItem(properties(FERTILIZED_EGG_ID).stacksTo(1));
    // Plain items: what they do to an animal comes from their food files. The tooltip is the default lore,
    // so crafted stacks show it too; it is translatable, so a pack that changes the food files can reword it.
    public static final Item BREEDERS_MASH = new Item(superfood(BREEDERS_MASH_ID));
    public static final Item GOLDEN_BRAN = new Item(superfood(GOLDEN_BRAN_ID));
    public static final Item DAIRY_MASH = new DairyMashItem(superfood(DAIRY_MASH_ID, false));
    // Drink like cow milk; the barnyard_romance:milk_buckets tag lets them stand in for it in cake.
    //? if >=1.21.2 {
    public static final Item SHEEP_MILK_BUCKET = new Item(milk(SHEEP_MILK_BUCKET_ID));
    public static final Item GOAT_MILK_BUCKET = new Item(milk(GOAT_MILK_BUCKET_ID));
    //?} else {
    /*public static final Item SHEEP_MILK_BUCKET = new MilkBucketItem(milk(SHEEP_MILK_BUCKET_ID));
    public static final Item GOAT_MILK_BUCKET = new MilkBucketItem(milk(GOAT_MILK_BUCKET_ID));
    *///?}

    private BarnyardItems() {
    }

    // Items know their id from their properties since 1.21.2; before that the registry names them.
    private static Item.Properties properties(ResourceKey<Item> id) {
        //? if >=1.21.2 {
        return new Item.Properties().setId(id);
        //?} else
        //return new Item.Properties();
    }

    private static Item.Properties milk(ResourceKey<Item> id) {
        return properties(id)
                .craftRemainder(Items.BUCKET)
                //? if >=1.21.2 {
                .component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET)
                .usingConvertsTo(Items.BUCKET)
                //?}
                .stacksTo(1);
    }

    private static Item.Properties superfood(ResourceKey<Item> id) {
        return superfood(id, true);
    }

    // A superfood whose effect line depends on the config writes it itself (see DairyMashItem).
    private static Item.Properties superfood(ResourceKey<Item> id, boolean effectLine) {
        String key = "item." + Constants.MOD_ID + "." + id.identifier().getPath();
        List<Component> lore = effectLine
                ? List.of(Component.translatable(key + ".use"), Component.translatable(key + ".effect"))
                : List.of(Component.translatable(key + ".use"));
        return properties(id)
                .rarity(Rarity.UNCOMMON)
                .component(DataComponents.LORE, new ItemLore(lore));
    }

    // Our items that act on an animal before it does, so a horse doesn't rear and a tamed cat doesn't sit instead.
    public static boolean isTool(ItemStack stack) {
        return stack.is(HERD_STICK) || stack.is(INSPECTOR) || stack.is(HERD_BOOK);
    }
}
