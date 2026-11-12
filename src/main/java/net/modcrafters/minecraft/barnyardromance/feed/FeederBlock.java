package net.modcrafters.minecraft.barnyardromance.feed;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
        BlockState initial = stateDefinition.any();
        registerDefaultState(initial.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? initial.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH) : initial);
    }

    public FeederKind kind() {
        return kind;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite()) : state;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING))) : state;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING))) : state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    //? if <1.21.2 {
    /*@Override
    protected MapCodec<? extends FeederBlock> codec() {
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
        if (!state.is(newState.getBlock())) {
            Containers.dropContentsOnDestroy(state, newState, level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    *///?}

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // The outline follows each model. The Trough is narrower across its facing; the Manger's slight overhang is left out.
    private static final VoxelShape BIRD_FEEDER = Block.box(0.0, 0.0, 0.0, 16.0, 8.125, 16.0);
    private static final VoxelShape TROUGH_NORTH_SOUTH = Block.box(0.0, 0.0, 2.0, 16.0, 11.375, 14.0);
    private static final VoxelShape TROUGH_EAST_WEST = Block.box(2.0, 0.0, 0.0, 14.0, 11.375, 16.0);
    private static final VoxelShape FEEDER = Block.box(0.0, 0.0, 0.0, 16.0, 14.875, 16.0);
    private static final VoxelShape MANGER = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape PET_BOWL = Block.box(1.5, 0.0, 1.5, 14.5, 5.625, 14.5);
    // The tall ones are as high as a fence, so livestock walking off from a meal (or crowding in for one) can't jump
    // up onto them. The low ones are as their models: a chicken may hop onto its feeder and you can step over a bowl.
    private static final VoxelShape FENCE_HIGH = Block.box(0.0, 0.0, 0.0, 16.0, 24.0, 16.0);

    private VoxelShape outline(BlockState state) {
        return switch (kind) {
            case BIRD_FEEDER -> BIRD_FEEDER;
            case TROUGH -> state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                    && state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis() == Direction.Axis.X
                    ? TROUGH_EAST_WEST : TROUGH_NORTH_SOUTH;
            case FEEDER -> FEEDER;
            case MANGER -> MANGER;
            case PET_BOWL -> PET_BOWL;
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return outline(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return kind == FeederKind.MANGER || kind == FeederKind.FEEDER ? FENCE_HIGH : outline(state);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FeederBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, FeedBlocks.FEEDER_ENTITY, FeederBlockEntity::serverTick);
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
