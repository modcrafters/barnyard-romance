package net.modcrafters.minecraft.barnyardromance.eggs;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;
import net.modcrafters.minecraft.barnyardromance.core.registry.BlockProperties;

// The nesting box: its block, block item, block entity and window. They are made when this class loads, which is
// while the registries are open (see register).
public final class EggsBlocks {
    public static final Identifier NESTING_BOX_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "nesting_box");
    public static final Block NESTING_BOX = new NestingBoxBlock(BlockProperties.of(NESTING_BOX_ID)
            .mapColor(MapColor.WOOD)
            .strength(2.0F)
            .sound(SoundType.WOOD)
            .ignitedByLava()
            .noOcclusion());
    public static final Item NESTING_BOX_ITEM = new BlockItem(NESTING_BOX, BlockProperties.item(NESTING_BOX_ID));
    public static final BlockEntityType<NestingBoxBlockEntity> NESTING_BOX_ENTITY = Services.PLATFORM.blockEntityType(NestingBoxBlockEntity::new, NESTING_BOX);
    public static final MenuType<NestingBoxMenu> NESTING_BOX_MENU = Services.PLATFORM.menuType(NestingBoxMenu::new);

    private EggsBlocks() {
    }

    static void register(BarnyardRegistrations.Sink sink) {
        sink.register(Registries.BLOCK, NESTING_BOX_ID, () -> NESTING_BOX);
        sink.register(Registries.ITEM, NESTING_BOX_ID, () -> NESTING_BOX_ITEM);
        sink.register(Registries.BLOCK_ENTITY_TYPE, NESTING_BOX_ID, () -> NESTING_BOX_ENTITY);
        sink.register(Registries.MENU, NESTING_BOX_ID, () -> NESTING_BOX_MENU);
    }
}
