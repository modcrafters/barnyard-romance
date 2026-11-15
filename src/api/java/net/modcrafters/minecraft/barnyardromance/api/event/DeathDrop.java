package net.modcrafters.minecraft.barnyardromance.api.event;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.api.Cancellable;

// One stack a tracked animal drops when it dies, already sized by its size, sex and age. Cancelled, that stack isn't
// dropped. A count over the stack size drops as several stacks.
public final class DeathDrop implements Cancellable {
    private final Animal animal;
    private ItemStack stack;
    private boolean cancelled;

    public DeathDrop(Animal animal, ItemStack stack) {
        this.animal = animal;
        this.stack = stack;
    }

    public Animal animal() {
        return animal;
    }

    public ItemStack stack() {
        return stack;
    }

    // Not empty: cancel() drops nothing.
    public void setStack(ItemStack stack) {
        this.stack = Stacks.notEmpty(stack);
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
