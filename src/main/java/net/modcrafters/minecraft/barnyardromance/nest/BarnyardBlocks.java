package net.modcrafters.minecraft.barnyardromance.nest;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.feed.FeederBlock;
import net.modcrafters.minecraft.barnyardromance.feed.FeederBlockEntity;
import net.modcrafters.minecraft.barnyardromance.feed.FeederKind;
import net.modcrafters.minecraft.barnyardromance.feed.FeederMenu;
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

// The nesting box and the five feeding blocks: their blocks, block items, block entities and windows. Each loader
// registers them under these ids.
public final class BarnyardBlocks {
    public static final Identifier NESTING_BOX_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "nesting_box");
    public static final Block NESTING_BOX = new NestingBoxBlock(blockProperties(NESTING_BOX_ID)
            .mapColor(MapColor.WOOD)
            .strength(2.0F)
            .sound(SoundType.WOOD)
            .ignitedByLava()
            .noOcclusion());
    public static final Item NESTING_BOX_ITEM = new BlockItem(NESTING_BOX, itemProperties(NESTING_BOX_ID));
    public static final BlockEntityType<NestingBoxBlockEntity> NESTING_BOX_ENTITY = Services.PLATFORM.blockEntityType(NestingBoxBlockEntity::new, NESTING_BOX);
    public static final MenuType<NestingBoxMenu> NESTING_BOX_MENU = Services.PLATFORM.menuType(NestingBoxMenu::new);

    public static final Block BIRD_FEEDER = feeder(FeederKind.BIRD_FEEDER);
    public static final Block TROUGH = feeder(FeederKind.TROUGH);
    public static final Block FEEDER = feeder(FeederKind.FEEDER);
    public static final Block MANGER = feeder(FeederKind.MANGER);
    public static final Block PET_BOWL = feeder(FeederKind.PET_BOWL);
    public static final Item BIRD_FEEDER_ITEM = new BlockItem(BIRD_FEEDER, itemProperties(id(FeederKind.BIRD_FEEDER)));
    public static final Item TROUGH_ITEM = new BlockItem(TROUGH, itemProperties(id(FeederKind.TROUGH)));
    public static final Item FEEDER_ITEM = new BlockItem(FEEDER, itemProperties(id(FeederKind.FEEDER)));
    public static final Item MANGER_ITEM = new BlockItem(MANGER, itemProperties(id(FeederKind.MANGER)));
    public static final Item PET_BOWL_ITEM = new BlockItem(PET_BOWL, itemProperties(id(FeederKind.PET_BOWL)));
    // All five share one block entity and one window; the block says which kind it is.
    public static final Identifier FEEDING_BLOCK_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "feeding_block");
    public static final BlockEntityType<FeederBlockEntity> FEEDER_ENTITY = Services.PLATFORM.blockEntityType(FeederBlockEntity::new,
            BIRD_FEEDER, TROUGH, FEEDER, MANGER, PET_BOWL);
    public static final MenuType<FeederMenu> FEEDER_MENU = Services.PLATFORM.menuType(FeederMenu::new);

    private BarnyardBlocks() {
    }

    public static Identifier id(FeederKind kind) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, kind.id());
    }

    public static Block block(FeederKind kind) {
        return switch (kind) {
            case BIRD_FEEDER -> BIRD_FEEDER;
            case TROUGH -> TROUGH;
            case FEEDER -> FEEDER;
            case MANGER -> MANGER;
            case PET_BOWL -> PET_BOWL;
        };
    }

    public static Item item(FeederKind kind) {
        return switch (kind) {
            case BIRD_FEEDER -> BIRD_FEEDER_ITEM;
            case TROUGH -> TROUGH_ITEM;
            case FEEDER -> FEEDER_ITEM;
            case MANGER -> MANGER_ITEM;
            case PET_BOWL -> PET_BOWL_ITEM;
        };
    }

    private static Block feeder(FeederKind kind) {
        return new FeederBlock(kind, blockProperties(id(kind))
                .mapColor(MapColor.WOOD)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .ignitedByLava());
    }

    // Blocks and items know their id from their properties since 1.21.2; before that the registry names them.
    private static BlockBehaviour.Properties blockProperties(Identifier id) {
        //? if >=1.21.2 {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id));
        //?} else
        //return BlockBehaviour.Properties.of();
    }

    private static Item.Properties itemProperties(Identifier id) {
        //? if >=1.21.2 {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix();
        //?} else
        //return new Item.Properties();
    }
}
