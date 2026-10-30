package net.modcrafters.minecraft.barnyardromance.nest;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

// The nesting box: its block, block item, block entity and window. Each loader registers them under these ids.
public final class BarnyardBlocks {
    public static final Identifier NESTING_BOX_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "nesting_box");
    public static final Block NESTING_BOX = new NestingBoxBlock(blockProperties()
            .mapColor(MapColor.WOOD)
            .strength(2.0F)
            .sound(SoundType.WOOD)
            .ignitedByLava()
            .noOcclusion());
    public static final Item NESTING_BOX_ITEM = new BlockItem(NESTING_BOX, itemProperties());
    public static final BlockEntityType<NestingBoxBlockEntity> NESTING_BOX_ENTITY = Services.PLATFORM.blockEntityType(NestingBoxBlockEntity::new, NESTING_BOX);
    public static final MenuType<NestingBoxMenu> NESTING_BOX_MENU = Services.PLATFORM.menuType(NestingBoxMenu::new);

    private BarnyardBlocks() {
    }

    // Blocks and items know their id from their properties since 1.21.2; before that the registry names them.
    private static BlockBehaviour.Properties blockProperties() {
        //? if >=1.21.2 {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, NESTING_BOX_ID));
        //?} else
        //return BlockBehaviour.Properties.of();
    }

    private static Item.Properties itemProperties() {
        //? if >=1.21.2 {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, NESTING_BOX_ID)).useBlockDescriptionPrefix();
        //?} else
        //return new Item.Properties();
    }
}
