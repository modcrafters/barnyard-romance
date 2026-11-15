package net.modcrafters.minecraft.barnyardromance.api.event;

import net.minecraft.world.item.ItemStack;

/**
 * The check every stack-changing event shares: nothing is given by cancelling, never by an empty stack.
 */
final class Stacks {
    private Stacks() {
    }

    static ItemStack notEmpty(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException("use cancel() to give nothing");
        }
        return stack;
    }
}
