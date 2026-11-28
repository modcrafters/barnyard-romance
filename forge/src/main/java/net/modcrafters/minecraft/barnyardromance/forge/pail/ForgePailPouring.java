package net.modcrafters.minecraft.barnyardromance.forge.pail;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailPouring;

import java.util.Optional;

// The pail window's pour through Forge's fluid capability, ported from NeoForge's 1.21.1 one. Each side is a copy, so
// the capability changes only the copies, and the pour hands back what they became.
public final class ForgePailPouring implements PailPouring {
    @Override
    public Optional<Poured> pour(ItemStack container, ItemStack pail) {
        Optional<IFluidHandlerItem> from = FluidUtil.getFluidHandler(container.copyWithCount(1)).resolve();
        Optional<IFluidHandlerItem> to = FluidUtil.getFluidHandler(pail.copyWithCount(1)).resolve();
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
}
