package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
//? if >=1.21.5 {
import net.minecraft.server.level.ServerLevel;
//?} else
//import net.minecraft.world.Containers;
//? if <1.21.2 {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
*///?}

// A Bird Feeder, Trough, Feeder, Manger or Pet Bowl: a store of plain food for the animals of its kind. A right click
// opens it; breaking it drops what's inside.
public class FeederBlock extends BaseEntityBlock {
    //? if <1.21.2 {
    /*public static final MapCodec<FeederBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.xmap(FeederKind::valueOf, FeederKind::name).fieldOf("kind").forGetter(FeederBlock::kind),
            propertiesCodec()
    ).apply(instance, FeederBlock::new));
    *///?}

    private final FeederKind kind;

    public FeederBlock(FeederKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public FeederKind kind() {
        return kind;
    }

    //? if <1.21.2 {
    /*@Override
    protected MapCodec<FeederBlock> codec() {
        return CODEC;
    }
    *///?}

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FeederBlockEntity feeder) {
            player.openMenu(feeder);
        }
        return InteractionResult.SUCCESS;
    }

    // Since 1.21.5 the block entity drops its food itself and the block only tells comparators it's gone; before
    // that the block drops the food.
    //? if >=1.21.5 {
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        net.minecraft.world.Containers.updateNeighboursAfterDestroy(state, level, pos);
    }
    //?} else {
    /*@Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    *///?}

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // As high as a fence, so an animal walking off from its meal (or crowding in for one) can't step up onto it.
    private static final VoxelShape COLLISION = Block.box(0.0, 0.0, 0.0, 16.0, 24.0, 16.0);

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FeederBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, BarnyardBlocks.FEEDER_ENTITY, FeederBlockEntity::serverTick);
    }

    // A comparator reads how full it is, as it reads a chest: a farm can be told when a block runs low.
    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    //? if >=1.21.2 {
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
    //?} else {
    /*@Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
    *///?}
}
