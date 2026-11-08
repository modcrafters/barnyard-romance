package net.modcrafters.minecraft.barnyardromance.feed;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
//? if <1.21.2
//import com.mojang.serialization.MapCodec;

// The server's hay level selects a baked model on every client; no inventory packets or renderer needed.
public final class MangerBlock extends FeederBlock {
    public static final IntegerProperty HAY_LEVEL = IntegerProperty.create("hay_level", 0, FeederFill.LEVELS);
    //? if <1.21.2
    //public static final MapCodec<MangerBlock> CODEC = simpleCodec(MangerBlock::new);

    public MangerBlock(Properties properties) {
        super(FeederKind.MANGER, properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH).setValue(HAY_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAY_LEVEL);
    }

    //? if <1.21.2 {
    /*@Override
    protected MapCodec<MangerBlock> codec() {
        return CODEC;
    }
    *///?}
}
