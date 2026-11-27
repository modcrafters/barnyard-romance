package net.modcrafters.minecraft.barnyardromance.forge.pail;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluids;
import org.jspecify.annotations.Nullable;

import java.util.List;

// A full bucket of goat or sheep milk, which empties into a tank all at once and leaves an empty bucket. Forge's own
// bucket handler takes every MilkBucketItem for Forge's milk, and the mod's milk buckets are MilkBucketItems on 1.20.1,
// so they get this one instead (see MilkBucketItemMixin). Like Forge's for a milk bucket, it fills nothing.
public final class MilkBucketFluidHandler extends FluidBucketWrapper {
    private final Item bucket;
    private final Fluid milk;

    public MilkBucketFluidHandler(ItemStack container, Item bucket, Fluid milk) {
        super(container);
        this.bucket = bucket;
        this.milk = milk;
    }

    // The handler for one of the mod's milk buckets, or null for any other milk bucket (vanilla's keeps Forge's).
    public static @Nullable MilkBucketFluidHandler forBucket(ItemStack stack) {
        // Only the mod's own items read MilkFluids, which mustn't load before the registries open.
        if (!Constants.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace())) {
            return null;
        }
        for (Holder<Fluid> milk : List.of(MilkFluids.GOAT, MilkFluids.SHEEP)) {
            Item bucket = milk.value().getBucket();
            if (stack.is(bucket)) {
                return new MilkBucketFluidHandler(stack, bucket, milk.value());
            }
        }
        return null;
    }

    @Override
    public FluidStack getFluid() {
        return container.is(bucket) ? new FluidStack(milk, FluidType.BUCKET_VOLUME) : FluidStack.EMPTY;
    }
}
