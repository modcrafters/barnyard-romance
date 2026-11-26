package net.modcrafters.minecraft.barnyardromance.fabric.pail;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailPouring;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// The pail window's pour through Fabric's Transfer API. Each side is a copy in a one-slot container, so the storage
// changes only the copies and can swap the item, as a bucket does when it fills. The pail's storage takes only whole
// millilitres, and StorageUtil.move takes from the source exactly what the target took.
public final class FabricPailPouring implements PailPouring {
    @Override
    public Optional<Poured> pour(ItemStack container, ItemStack pail) {
        OneSlot from = OneSlot.of(container);
        OneSlot to = OneSlot.of(pail);
        if (from.storage == null || to.storage == null) {
            return Optional.empty();
        }
        // A container that holds no fluid fills from the pail; any other pours into it.
        boolean fill = !from.storage.nonEmptyIterator().hasNext();
        long moved;
        try (Transaction transaction = Transaction.openOuter()) {
            moved = fill
                    ? StorageUtil.move(to.storage, from.storage, variant -> true, Long.MAX_VALUE, transaction)
                    : StorageUtil.move(from.storage, to.storage, variant -> true, Long.MAX_VALUE, transaction);
            transaction.commit();
        }
        return moved == 0 ? Optional.empty() : Optional.of(new Poured(from.stack(), PailItem.contents(to.stack())));
    }

    private record OneSlot(SimpleContainer slot, @Nullable Storage<FluidVariant> storage) {
        static OneSlot of(ItemStack stack) {
            SimpleContainer slot = new SimpleContainer(stack.copyWithCount(1));
            ContainerItemContext context = ContainerItemContext.ofSingleSlot(ContainerStorage.of(slot, null).getSlot(0));
            return new OneSlot(slot, context.find(FluidStorage.ITEM));
        }

        ItemStack stack() {
            return slot.getItem(0);
        }
    }
}
