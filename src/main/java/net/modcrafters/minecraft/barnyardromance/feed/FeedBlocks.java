package net.modcrafters.minecraft.barnyardromance.feed;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;
import net.modcrafters.minecraft.barnyardromance.core.registry.BlockProperties;

// The five feeding blocks: their blocks, block items, block entity and window. They are made when this class loads,
// which is while the registries are open (see register).
public final class FeedBlocks {
    public static final Block BIRD_FEEDER = feeder(FeederKind.BIRD_FEEDER);
    public static final Block TROUGH = feeder(FeederKind.TROUGH);
    public static final Block FEEDER = feeder(FeederKind.FEEDER);
    public static final Block MANGER = feeder(FeederKind.MANGER);
    public static final Block PET_BOWL = feeder(FeederKind.PET_BOWL);
    public static final Item BIRD_FEEDER_ITEM = new BlockItem(BIRD_FEEDER, BlockProperties.item(id(FeederKind.BIRD_FEEDER)));
    public static final Item TROUGH_ITEM = new BlockItem(TROUGH, BlockProperties.item(id(FeederKind.TROUGH)));
    public static final Item FEEDER_ITEM = new BlockItem(FEEDER, BlockProperties.item(id(FeederKind.FEEDER)));
    public static final Item MANGER_ITEM = new BlockItem(MANGER, BlockProperties.item(id(FeederKind.MANGER)));
    public static final Item PET_BOWL_ITEM = new BlockItem(PET_BOWL, BlockProperties.item(id(FeederKind.PET_BOWL)));
    // All five share one block entity and one window; the block says which kind it is.
    public static final Identifier FEEDING_BLOCK_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "feeding_block");
    public static final BlockEntityType<FeederBlockEntity> FEEDER_ENTITY = Services.PLATFORM.blockEntityType(FeederBlockEntity::new,
            BIRD_FEEDER, TROUGH, FEEDER, MANGER, PET_BOWL);
    public static final MenuType<FeederMenu> FEEDER_MENU = Services.PLATFORM.menuType(FeederMenu::new);

    private FeedBlocks() {
    }

    static void register(BarnyardRegistrations.Sink sink) {
        for (FeederKind kind : FeederKind.values()) {
            sink.register(Registries.BLOCK, id(kind), () -> block(kind));
            sink.register(Registries.ITEM, id(kind), () -> item(kind));
        }
        // Fabric's hoppers and pipes reach the feeding blocks through the Transfer API's fallback for vanilla containers,
        // NeoForge's through the item capability (see BarnyardRomanceNeoForge.registerCapabilities).
        sink.register(Registries.BLOCK_ENTITY_TYPE, FEEDING_BLOCK_ID, () -> FEEDER_ENTITY);
        sink.register(Registries.MENU, FEEDING_BLOCK_ID, () -> FEEDER_MENU);
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
        if (kind == FeederKind.PET_BOWL) {
            return new PetBowlBlock(BlockProperties.of(id(kind)).mapColor(MapColor.COLOR_RED)
                    .strength(2.0F).sound(SoundType.STONE).noOcclusion());
        }
        BlockBehaviour.Properties properties = BlockProperties.of(id(kind))
                .mapColor(MapColor.WOOD)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .ignitedByLava();
        // The open rack and raised floor must not hide neighboring block faces.
        if (kind == FeederKind.MANGER) {
            return new MangerBlock(properties.noOcclusion());
        }
        if (kind == FeederKind.BIRD_FEEDER) {
            return new BirdFeederBlock(properties.noOcclusion());
        }
        if (kind == FeederKind.FEEDER || kind == FeederKind.TROUGH) {
            return new GrainFeederBlock(kind, properties.noOcclusion());
        }
        return new FeederBlock(kind, properties);
    }
}
