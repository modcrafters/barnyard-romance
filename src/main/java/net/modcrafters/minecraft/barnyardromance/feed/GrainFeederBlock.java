package net.modcrafters.minecraft.barnyardromance.feed;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
//? if <1.21.2
//import com.mojang.serialization.MapCodec;
//? if <1.21.2 {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
*///?}

// The server's food level selects a baked model on every client; no inventory packets or renderer needed.
public class GrainFeederBlock extends FeederBlock {
    public static final IntegerProperty FOOD_LEVEL = IntegerProperty.create("food_level", 0, FeederFill.LEVELS);
    //? if <1.21.2 {
    /*public static final MapCodec<GrainFeederBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.xmap(FeederKind::valueOf, FeederKind::name).fieldOf("kind").forGetter(FeederBlock::kind),
            propertiesCodec()
    ).apply(instance, GrainFeederBlock::new));
    *///?}

    public GrainFeederBlock(FeederKind kind, Properties properties) {
        super(kind, properties);
        registerDefaultState(defaultBlockState().setValue(FOOD_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FOOD_LEVEL);
    }

    //? if <1.21.2 {
    /*@Override
    protected MapCodec<? extends GrainFeederBlock> codec() {
        return CODEC;
    }
    *///?}
}
