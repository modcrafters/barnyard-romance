package net.modcrafters.minecraft.barnyardromance.feed;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
//? if <1.21.2
//import com.mojang.serialization.MapCodec;

// Four identical sides: only the seed level is saved, never a placement direction.
public final class BirdFeederBlock extends GrainFeederBlock {
    //? if <1.21.2
    //public static final MapCodec<BirdFeederBlock> CODEC = simpleCodec(BirdFeederBlock::new);

    public BirdFeederBlock(Properties properties) {
        super(FeederKind.BIRD_FEEDER, properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FOOD_LEVEL);
    }

    //? if <1.21.2 {
    /*@Override
    protected MapCodec<BirdFeederBlock> codec() {
        return CODEC;
    }
    *///?}
}
