package net.modcrafters.minecraft.barnyardromance.forge.pail;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailTransfer;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// A pail as Forge's fluid capability, for other mods' tanks and pipes, ported from NeoForge's 1.21.1 handler. A
// millibucket is a millilitre, and every amount goes through PailTransfer, so the tag check and the capacity are the
// pail's own. A fluid with a tag (a potion's, say) is refused, as the pail would lose it.
public final class PailFluidHandler implements IFluidHandlerItem, ICapabilityProvider {
    private final ItemStack container;
    private final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);

    public PailFluidHandler(ItemStack container) {
        this.container = container;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        // A fluid that isn't registered any more can't be named, so other mods see an empty pail.
        return PailTransfer.fluid(container)
                .map(fluid -> new FluidStack(fluid, PailItem.contents(container).amount()))
                .orElse(FluidStack.EMPTY);
    }

    @Override
    public int getTankCapacity(int tank) {
        return container.getItem() instanceof PailItem item ? item.capacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return !stack.hasTag() && PailFluids.accepts(stack.getFluid());
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || resource.hasTag()) {
            return 0;
        }
        return PailTransfer.insert(container, resource.getFluid(), resource.getAmount(), action.simulate());
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || resource.hasTag()) {
            return FluidStack.EMPTY;
        }
        int moved = PailTransfer.extract(container, resource.getFluid(), resource.getAmount(), action.simulate());
        return moved == 0 ? FluidStack.EMPTY : new FluidStack(resource.getFluid(), moved);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        Optional<Fluid> fluid = PailTransfer.fluid(container);
        if (fluid.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int moved = PailTransfer.extract(container, fluid.get(), maxDrain, action.simulate());
        return moved == 0 ? FluidStack.EMPTY : new FluidStack(fluid.get(), moved);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(capability, holder);
    }
}
