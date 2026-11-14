package net.modcrafters.minecraft.barnyardromance.feed;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// A feeding block's window: one chest row of food, a line saying how long it lasts, then the player's inventory.
// It is laid out like a one-row chest with a strip added under the row for that line.
public class FeederMenu extends AbstractContainerMenu {
    // The values the server sends: days of food, then the herd's size.
    public static final int DAYS = 0;
    public static final int ANIMALS = 1;
    public static final int DATA = 2;

    public static final int ROW_Y = 18;
    public static final int LINE_Y = ROW_Y + 21;
    // The strip added under the row.
    public static final int EXTRA = 12;
    public static final int INVENTORY_Y = ROW_Y + 18 + 13 + EXTRA;
    public static final int WIDTH = 176;
    public static final int HEIGHT = 114 + 18 + EXTRA;

    private final Container feeder;
    private final ContainerData herd;

    // The client's copy, filled in by the server.
    public FeederMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(FeederBlockEntity.SLOTS), new SimpleContainerData(DATA));
    }

    public FeederMenu(int containerId, Inventory inventory, FeederBlockEntity feeder) {
        this(containerId, inventory, feeder, feeder.windowData());
    }

    public FeederMenu(int containerId, Inventory inventory, Container feeder, ContainerData herd) {
        super(FeedBlocks.FEEDER_MENU, containerId);
        checkContainerSize(feeder, FeederBlockEntity.SLOTS);
        checkContainerDataCount(herd, DATA);
        this.feeder = feeder;
        this.herd = herd;
        feeder.startOpen(inventory.player);
        for (int slot = 0; slot < FeederBlockEntity.SLOTS; slot++) {
            this.addSlot(new FoodSlot(feeder, slot, 8 + slot * 18, ROW_Y));
        }
        this.addDataSlots(herd);
        //? if >=1.21.2 {
        this.addStandardInventorySlots(inventory, 8, INVENTORY_Y);
        //?} else {
        /*for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, INVENTORY_Y + 58));
        }
        *///?}
    }

    public int days() {
        return herd.get(DAYS);
    }

    public int animals() {
        return herd.get(ANIMALS);
    }

    // Every item in the food slots.
    public int food() {
        int food = 0;
        for (int slot = 0; slot < FeederBlockEntity.SLOTS; slot++) {
            food += this.slots.get(slot).getItem().getCount();
        }
        return food;
    }

    @Override
    public boolean stillValid(Player player) {
        return feeder.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack moved = stack.copy();
        int food = FeederBlockEntity.SLOTS;
        if (index < food) {
            if (!this.moveItemStackTo(stack, food, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, food, false)) {
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
        feeder.stopOpen(player);
    }

    // A food slot takes what the block takes.
    static class FoodSlot extends Slot {
        FoodSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container.canPlaceItem(getContainerSlot(), stack);
        }
    }
}
