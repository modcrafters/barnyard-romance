package net.modcrafters.minecraft.barnyardromance.neoforge.pail;

import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailPouring;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
//? if >=1.21.9 {
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
//?} else {
/*import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
*///?}

// The pail window's pour through NeoForge's fluid capability. Each side is a copy, so the capability changes only the
// copies, and the pour hands back what they became.
public final class NeoForgePailPouring implements PailPouring {
    //? if >=1.21.9 {
    @Override
    public Optional<Poured> pour(ItemStack container, ItemStack pail) {
        OneSlot from = OneSlot.of(container);
        OneSlot to = OneSlot.of(pail);
        if (from.handler == null || to.handler == null) {
            return Optional.empty();
        }
        // A container that holds no fluid fills from the pail; any other pours into it.
        boolean fill = ResourceHandlerUtil.isEmpty(from.handler);
        int moved;
        try (Transaction transaction = Transaction.openRoot()) {
            moved = fill
                    ? ResourceHandlerUtil.move(to.handler, from.handler, resource -> true, Integer.MAX_VALUE, transaction)
                    : ResourceHandlerUtil.move(from.handler, to.handler, resource -> true, Integer.MAX_VALUE, transaction);
            transaction.commit();
        }
        return moved == 0 ? Optional.empty() : Optional.of(new Poured(from.stack(), PailItem.contents(to.stack())));
    }

    // One item in a one-slot handler, so the capability can swap it for another item, as a bucket does when it fills.
    private record OneSlot(ItemStacksResourceHandler slot, @Nullable ResourceHandler<FluidResource> handler) {
        static OneSlot of(ItemStack stack) {
            ItemStacksResourceHandler slot = new ItemStacksResourceHandler(NonNullList.of(ItemStack.EMPTY, stack.copyWithCount(1)));
            return new OneSlot(slot, ItemAccess.forHandlerIndexStrict(slot, 0).getCapability(Capabilities.Fluid.ITEM));
        }

        ItemStack stack() {
            return slot.getResource(0).toStack((int) slot.getAmountAsLong(0));
        }
    }
    //?} else {
    /*@Override
    public Optional<Poured> pour(ItemStack container, ItemStack pail) {
        Optional<IFluidHandlerItem> from = FluidUtil.getFluidHandler(container.copyWithCount(1));
        Optional<IFluidHandlerItem> to = FluidUtil.getFluidHandler(pail.copyWithCount(1));
        if (from.isEmpty() || to.isEmpty()) {
            return Optional.empty();
        }
        // A container that holds no fluid fills from the pail; any other pours into it.
        FluidStack moved = isEmpty(from.get())
                ? FluidUtil.tryFluidTransfer(from.get(), to.get(), Integer.MAX_VALUE, true)
                : FluidUtil.tryFluidTransfer(to.get(), from.get(), Integer.MAX_VALUE, true);
        return moved.isEmpty() ? Optional.empty()
                : Optional.of(new Poured(from.get().getContainer(), PailItem.contents(to.get().getContainer())));
    }

    private static boolean isEmpty(IFluidHandlerItem handler) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            if (!handler.getFluidInTank(tank).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    *///?}
}
