package net.modcrafters.minecraft.barnyardromance.core.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

// The properties the mod's blocks and their block items start from, for every feature that registers one.
public final class BlockProperties {
    private BlockProperties() {
    }

    // Blocks and items know their id from their properties since 1.21.2; before that the registry names them.
    public static BlockBehaviour.Properties of(Identifier id) {
        //? if >=1.21.2 {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id));
        //?} else
        //return BlockBehaviour.Properties.of();
    }

    public static Item.Properties item(Identifier id) {
        //? if >=1.21.2 {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix();
        //?} else
        //return new Item.Properties();
    }
}
