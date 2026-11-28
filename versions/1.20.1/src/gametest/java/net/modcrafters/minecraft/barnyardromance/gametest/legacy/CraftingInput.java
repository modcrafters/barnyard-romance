package net.modcrafters.minecraft.barnyardromance.gametest.legacy;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// 1.21's CraftingInput, which the shared game tests import in its place on 1.20.1 (see stonecutter.gradle.kts): a grid
// of stacks for a recipe to match. Recipes match a crafting container here, so it is one, in a menu nobody has open.
public final class CraftingInput extends TransientCraftingContainer {
    private CraftingInput(int width, int height, NonNullList<ItemStack> items) {
        super(new NoMenu(), width, height, items);
    }

    public static CraftingInput of(int width, int height, List<ItemStack> items) {
        NonNullList<ItemStack> grid = NonNullList.withSize(width * height, ItemStack.EMPTY);
        for (int slot = 0; slot < Math.min(items.size(), grid.size()); slot++) {
            grid.set(slot, items.get(slot));
        }
        return new CraftingInput(width, height, grid);
    }

    public List<ItemStack> items() {
        return getItems();
    }

    // The menu a crafting container tells when its slots change.
    private static final class NoMenu extends AbstractContainerMenu {
        private NoMenu() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }
    }
}
