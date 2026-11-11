package net.modcrafters.minecraft.barnyardromance.milk;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;
import net.modcrafters.minecraft.barnyardromance.core.registry.ItemProperties;
//? if >=1.21.2 {
import net.minecraft.world.item.component.Consumables;
//?} else
//import net.minecraft.world.item.MilkBucketItem;

// Milk's items: Dairy Mash, and the milk sheep and goats give. The items are made when this class loads, which is
// while the registries are open (see register).
public final class MilkItems {
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
    public static final Item DAIRY_MASH = new DairyMashItem(ItemProperties.superfood(DAIRY_MASH_ID, false));
    // Drink like cow milk; the barnyard_romance:milk_buckets tag lets them stand in for it in cake.
    //? if >=1.21.2 {
    public static final Item SHEEP_MILK_BUCKET = new Item(milk(SHEEP_MILK_BUCKET_ID));
    public static final Item GOAT_MILK_BUCKET = new Item(milk(GOAT_MILK_BUCKET_ID));
    //?} else {
    /*public static final Item SHEEP_MILK_BUCKET = new MilkBucketItem(milk(SHEEP_MILK_BUCKET_ID));
    public static final Item GOAT_MILK_BUCKET = new MilkBucketItem(milk(GOAT_MILK_BUCKET_ID));
    *///?}

    private MilkItems() {
    }

    static void register(BarnyardRegistrations.Sink sink) {
        sink.register(Registries.ITEM, DAIRY_MASH_ID.identifier(), () -> DAIRY_MASH);
        sink.register(Registries.ITEM, SHEEP_MILK_BUCKET_ID.identifier(), () -> SHEEP_MILK_BUCKET);
        sink.register(Registries.ITEM, GOAT_MILK_BUCKET_ID.identifier(), () -> GOAT_MILK_BUCKET);
    }

    private static Item.Properties milk(ResourceKey<Item> id) {
        return ItemProperties.of(id)
                .craftRemainder(Items.BUCKET)
                //? if >=1.21.2 {
                .component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET)
                .usingConvertsTo(Items.BUCKET)
                //?}
                .stacksTo(1);
    }
}
