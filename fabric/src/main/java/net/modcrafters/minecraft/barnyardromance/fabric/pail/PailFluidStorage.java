package net.modcrafters.minecraft.barnyardromance.fabric.pail;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailTransfer;

// A pail as Fabric's Transfer API sees it, for other mods' tanks and pipes. Like SingleVariantItemStorage, it lives in
// its item's components and swaps the item in its context for a changed copy, but every amount goes through
// PailTransfer, so the tag check and the capacity are the pail's own. Fabric counts in droplets, 81 to the millilitre,
// and a pail keeps whole millilitres: the odd droplets of a request are refused, never rounded up into milk. A fluid
// with components is refused too, as the pail would lose them.
public final class PailFluidStorage implements SingleSlotStorage<FluidVariant> {
    private static final long DROPLETS_PER_MILLILITRE = FluidConstants.BUCKET / 1000;

    private final ContainerItemContext context;
    // The pail it was made for. If the context holds something else by now, the storage holds nothing.
    private final Item pail;

    public PailFluidStorage(ContainerItemContext context) {
        this.context = context;
        this.pail = context.getItemVariant().getItem();
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (resource.hasComponents() || !context.getItemVariant().isOf(pail)) {
            return 0;
        }
        ItemStack changed = context.getItemVariant().toStack();
        return exchanged(changed, PailTransfer.insert(changed, resource.getFluid(), millilitres(maxAmount), false), transaction);
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (resource.hasComponents() || !context.getItemVariant().isOf(pail)) {
            return 0;
        }
        ItemStack changed = context.getItemVariant().toStack();
        return exchanged(changed, PailTransfer.extract(changed, resource.getFluid(), millilitres(maxAmount), false), transaction);
    }

    @Override
    public boolean isResourceBlank() {
        return getResource().isBlank();
    }

    // A fluid that isn't registered any more can't be named, so other mods see an empty pail.
    @Override
    public FluidVariant getResource() {
        return PailTransfer.fluid(stack()).map(FluidVariant::of).orElse(FluidVariant.blank());
    }

    @Override
    public long getAmount() {
        ItemStack stack = stack();
        return PailTransfer.fluid(stack).isEmpty() ? 0 : PailItem.contents(stack).amount() * DROPLETS_PER_MILLILITRE;
    }

    @Override
    public long getCapacity() {
        return stack().getItem() instanceof PailItem item ? item.capacity() * DROPLETS_PER_MILLILITRE : 0;
    }

    // The droplets that moved, once the context holds the changed pail; none if it wouldn't take it.
    private long exchanged(ItemStack changed, int millilitres, TransactionContext transaction) {
        if (millilitres == 0 || context.exchange(ItemVariant.of(changed), 1, transaction) != 1) {
            return 0;
        }
        return millilitres * DROPLETS_PER_MILLILITRE;
    }

    // A copy of one pail, or an empty stack once the context holds something else.
    private ItemStack stack() {
        ItemVariant current = context.getItemVariant();
        return current.isOf(pail) ? current.toStack() : ItemStack.EMPTY;
    }

    private static int millilitres(long droplets) {
        return (int) Math.min(Integer.MAX_VALUE, droplets / DROPLETS_PER_MILLILITRE);
    }

    @Override
    public String toString() {
        return "PailFluidStorage[" + context + "/" + pail + "]";
    }
}
