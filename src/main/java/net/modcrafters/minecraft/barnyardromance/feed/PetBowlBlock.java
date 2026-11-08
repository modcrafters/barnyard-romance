package net.modcrafters.minecraft.barnyardromance.feed;

//? if <1.21.2
//import com.mojang.serialization.MapCodec;

// Opposite paw prints match: the dog sides follow the saved facing, with cat prints on the other axis.
public final class PetBowlBlock extends GrainFeederBlock {
    //? if <1.21.2
    //public static final MapCodec<PetBowlBlock> CODEC = simpleCodec(PetBowlBlock::new);

    public PetBowlBlock(Properties properties) {
        super(FeederKind.PET_BOWL, properties);
    }

    //? if <1.21.2 {
    /*@Override
    protected MapCodec<PetBowlBlock> codec() {
        return CODEC;
    }
    *///?}
}
