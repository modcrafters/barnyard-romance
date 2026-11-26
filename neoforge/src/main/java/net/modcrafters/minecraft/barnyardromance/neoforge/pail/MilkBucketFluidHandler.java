package net.modcrafters.minecraft.barnyardromance.neoforge.pail;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
//? if >=1.21.9 {
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
//?} else {
/*import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
*///?}

// A full bucket of goat or sheep milk, which empties into a tank all at once and leaves an empty bucket. NeoForge's
// own bucket handler only knows bucket items and vanilla's milk bucket, and fills an empty bucket with any fluid that
// names its bucket, so the mod's milks need only this.
//? if >=1.21.9 {
public final class MilkBucketFluidHandler extends ItemAccessResourceHandler<FluidResource> {
    private final Item bucket;
    private final FluidResource milk;

    public MilkBucketFluidHandler(ItemAccess access, Item bucket, Fluid milk) {
        super(access, 1);
        this.bucket = bucket;
        this.milk = FluidResource.of(milk);
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource resource, int index) {
        return resource.is(bucket) ? milk : FluidResource.EMPTY;
    }

    @Override
    protected int getAmountFrom(ItemResource resource, int index) {
        return resource.is(bucket) ? FluidType.BUCKET_VOLUME : 0;
    }

    // Only a whole bucket comes out, and nothing goes in: anything but empty is refused.
    @Override
    protected ItemResource update(ItemResource resource, int index, FluidResource newResource, int newAmount) {
        return newAmount == 0 ? ItemResource.of(Items.BUCKET) : ItemResource.EMPTY;
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return FluidType.BUCKET_VOLUME;
    }
}
//?} else {
/*public final class MilkBucketFluidHandler extends FluidBucketWrapper {
    private final Item bucket;
    private final Fluid milk;

    public MilkBucketFluidHandler(ItemStack container, Item bucket, Fluid milk) {
        super(container);
        this.bucket = bucket;
        this.milk = milk;
    }

    // NeoForge's wrapper takes every MilkBucketItem for its own milk, and the mod's milk buckets are MilkBucketItems
    // before 1.21.2.
    @Override
    public FluidStack getFluid() {
        return container.is(bucket) ? new FluidStack(milk, FluidType.BUCKET_VOLUME) : FluidStack.EMPTY;
    }
}
*///?}
