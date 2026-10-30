package net.modcrafters.minecraft.barnyardromance.nest;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// The nesting box window: ten nests (NestSlot), then the player's inventory and hotbar.
public class NestingBoxMenu extends AbstractContainerMenu {
    private final Container box;
    // The sitting hen's nest plus one, or 0 (NestingBoxBlockEntity.sitting).
    private final ContainerData sitting;

    // The client's copy, filled in by the server.
    public NestingBoxMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(NestingBoxBlockEntity.SLOTS), new SimpleContainerData(1));
    }

    public NestingBoxMenu(int containerId, Inventory inventory, NestingBoxBlockEntity box) {
        this(containerId, inventory, box, box.sitting());
    }

    public NestingBoxMenu(int containerId, Inventory inventory, Container box, ContainerData sitting) {
        super(BarnyardBlocks.NESTING_BOX_MENU, containerId);
        checkContainerSize(box, NestingBoxBlockEntity.SLOTS);
        checkContainerDataCount(sitting, 1);
        this.box = box;
        this.sitting = sitting;
        box.startOpen(inventory.player);
        for (int slot = 0; slot < NestingBoxBlockEntity.SLOTS; slot++) {
            this.addSlot(new NestSlot(this, box, slot, NestingBoxLayout.nestX(slot), NestingBoxLayout.nestY(slot)));
        }
        this.addDataSlots(sitting);
        //? if >=1.21.2 {
        this.addStandardInventorySlots(inventory, 8, NestingBoxLayout.INVENTORY_Y);
        //?} else {
        /*for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, NestingBoxLayout.INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, NestingBoxLayout.HOTBAR_Y));
        }
        *///?}
    }

    // The nest a hen is sitting in right now, or -1.
    public int sittingSlot() {
        return sitting.get(0) - 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return box.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack moved = stack.copy();
        int nests = NestingBoxBlockEntity.SLOTS;
        if (index < nests) {
            if (!this.moveItemStackTo(stack, nests, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!NestingBoxBlockEntity.allowedByHand(stack) || !this.moveItemStackTo(stack, 0, nests, false)) {
            // Shift-clicking would otherwise top up a pile of plain eggs, which only hens may add to.
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return moved;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        box.stopOpen(player);
    }

    // A nest: by hand only a fertilized egg goes in, one to a nest, and never in the nest a hen is sitting in.
    static class NestSlot extends Slot {
        private final NestingBoxMenu menu;

        NestSlot(NestingBoxMenu menu, Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return NestingBoxBlockEntity.allowedByHand(stack) && !hasItem() && menu.sittingSlot() != getContainerSlot();
        }
    }
}
