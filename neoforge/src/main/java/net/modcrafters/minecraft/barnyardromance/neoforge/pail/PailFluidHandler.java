package net.modcrafters.minecraft.barnyardromance.neoforge.pail;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.modcrafters.minecraft.barnyardromance.pail.PailFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailTransfer;

import java.util.Optional;
//? if >=1.21.9 {
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;
//?} else {
/*import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
*///?}

// A pail as NeoForge's fluid capability, for other mods' tanks and pipes. A millibucket is a millilitre, and every
// amount goes through PailTransfer, so the tag check and the capacity are the pail's own. A fluid with components
// (a potion's, say) is refused, as the pail would lose them.
//? if >=1.21.9 {
// The capability reaches the pail through an ItemAccess, which may be a player's hand or a slot in a machine, so the
// handler works on a copy of its stack and exchanges it for the changed copy within the transaction.
public final class PailFluidHandler implements ResourceHandler<FluidResource> {
    private final ItemAccess access;
    // The pail it was made for. If the access holds something else by now, the handler holds nothing.
    private final Item pail;

    public PailFluidHandler(ItemAccess access) {
        this.access = access;
        this.pail = access.getResource().getItem();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        Objects.checkIndex(index, size());
        return fluid().map(FluidResource::of).orElse(FluidResource.EMPTY);
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        // A fluid that isn't registered any more can't be named, so other mods see an empty pail.
        return fluid().isEmpty() ? 0 : (long) access.getAmount() * PailItem.contents(stack()).amount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        if (!(stack().getItem() instanceof PailItem item) || !(resource.isEmpty() || isValid(index, resource))) {
            return 0;
        }
        return (long) access.getAmount() * item.capacity();
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return resource.isComponentsPatchEmpty() && PailFluids.accepts(resource.getFluid());
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int pails = access.getAmount();
        if (pails == 0 || !resource.isComponentsPatchEmpty() || !access.getResource().is(pail)) {
            return 0;
        }
        ItemStack changed = stack();
        int moved = PailTransfer.insert(changed, resource.getFluid(), amount / pails, false);
        return moved == 0 ? 0 : moved * access.exchange(ItemResource.of(changed), pails, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int pails = access.getAmount();
        if (pails == 0 || !resource.isComponentsPatchEmpty() || !access.getResource().is(pail)) {
            return 0;
        }
        ItemStack changed = stack();
        int moved = PailTransfer.extract(changed, resource.getFluid(), amount / pails, false);
        return moved == 0 ? 0 : moved * access.exchange(ItemResource.of(changed), pails, transaction);
    }

    // A copy of one pail, or an empty stack once the access holds something else.
    private ItemStack stack() {
        ItemResource current = access.getResource();
        return current.is(pail) ? current.toStack() : ItemStack.EMPTY;
    }

    private Optional<Fluid> fluid() {
        return PailTransfer.fluid(stack());
    }
}
//?} else {
/*public final class PailFluidHandler implements IFluidHandlerItem {
    private final ItemStack container;

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
        return stack.isComponentsPatchEmpty() && PailFluids.accepts(stack.getFluid());
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !resource.isComponentsPatchEmpty()) {
            return 0;
        }
        return PailTransfer.insert(container, resource.getFluid(), resource.getAmount(), action.simulate());
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !resource.isComponentsPatchEmpty()) {
            return FluidStack.EMPTY;
        }
        int moved = PailTransfer.extract(container, resource.getFluid(), resource.getAmount(), action.simulate());
        return moved == 0 ? FluidStack.EMPTY : resource.copyWithAmount(moved);
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
}
*///?}
