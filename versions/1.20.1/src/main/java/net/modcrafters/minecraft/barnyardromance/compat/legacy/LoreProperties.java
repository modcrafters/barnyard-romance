package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;

// Item properties that carry an item's default lore, for LoreItem on Minecraft 1.20.1.
public class LoreProperties extends Item.Properties {
    private final List<Component> lore;

    public LoreProperties(List<Component> lore) {
        this.lore = List.copyOf(lore);
    }

    public List<Component> lore() {
        return lore;
    }
}
