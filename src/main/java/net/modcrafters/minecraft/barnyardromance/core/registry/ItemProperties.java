package net.modcrafters.minecraft.barnyardromance.core.registry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
import net.modcrafters.minecraft.barnyardromance.core.Constants;

import java.util.List;

// The properties the mod's items start from, for every feature that registers one.
public final class ItemProperties {
    private ItemProperties() {
    }

    // Items know their id from their properties since 1.21.2; before that the registry names them.
    public static Item.Properties of(ResourceKey<Item> id) {
        //? if >=1.21.2 {
        return new Item.Properties().setId(id);
        //?} else
        //return new Item.Properties();
    }

    // Plain items: what they do to an animal comes from their food files. The tooltip is the default lore, so crafted
    // stacks show it too; it is translatable, so a pack that changes the food files can reword it.
    public static Item.Properties superfood(ResourceKey<Item> id) {
        return superfood(id, true);
    }

    // A superfood whose effect line depends on the config writes it itself (see DairyMashItem).
    public static Item.Properties superfood(ResourceKey<Item> id, boolean effectLine) {
        String key = "item." + Constants.MOD_ID + "." + id.identifier().getPath();
        List<Component> lore = effectLine
                ? List.of(Component.translatable(key + ".use"), Component.translatable(key + ".effect"))
                : List.of(Component.translatable(key + ".use"));
        return of(id)
                .rarity(Rarity.UNCOMMON)
                .component(DataComponents.LORE, new ItemLore(lore));
    }
}
