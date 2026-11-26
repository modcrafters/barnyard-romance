package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

// A milk that lives only in pails, buckets and other mods' tanks: it has no block, so it can't be poured into the
// world, and it is always a full source. The loaders subclass it (see IPlatformHelper.milkFluid), as NeoForge needs a
// fluid type on it. The bucket is a supplier because fluids register before items.
public abstract class MilkFluid extends Fluid {
    private final Supplier<Item> bucket;
    private final int colour;

    protected MilkFluid(Supplier<Item> bucket, int colour) {
        this.bucket = bucket;
        this.colour = colour;
    }

    // The tint for its pail bar and its tank texture, as RGB.
    public int colour() {
        return colour;
    }

    @Override
    public Item getBucket() {
        return bucket.get();
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid other, Direction direction) {
        return true;
    }

    @Override
    protected Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
        return Vec3.ZERO;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
        return getOwnHeight(state);
    }

    @Override
    public float getOwnHeight(FluidState state) {
        return getAmount(state) / 9.0F;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSource(FluidState state) {
        return true;
    }

    @Override
    public int getAmount(FluidState state) {
        return 8;
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }
}
