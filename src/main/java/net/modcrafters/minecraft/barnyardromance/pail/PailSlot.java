package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// The pail window's slot. It takes one bucket that trades with the pail and trades it the moment it goes in, however
// it goes in: a click, a shift-click, a drag or a hotbar key all end in setByPlayer. A plain set, as the server's sync
// uses, never trades.
public class PailSlot extends Slot {
    private final PailMenu menu;

    PailSlot(PailMenu menu, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.menu = menu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return menu.trades(stack);
    }

    // One at a time, so a stack of empty buckets moves in one by one and the rest stay with the player.
    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setByPlayer(ItemStack stack, ItemStack previous) {
        super.setByPlayer(menu.trade(stack), previous);
    }
}
