package net.modcrafters.minecraft.barnyardromance.api.event;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.api.Cancellable;

/**
 * A player is about to milk an animal, and every rule has let them. result is what the bucket becomes. Cancelled,
 * nothing happens: the bucket stays a bucket and she counts as not milked today.
 */
public final class Milking implements Cancellable {
    private final Animal animal;
    private final Player player;
    private ItemStack result;
    private boolean cancelled;

    public Milking(Animal animal, Player player, ItemStack result) {
        this.animal = animal;
        this.player = player;
        this.result = result;
    }

    public Animal animal() {
        return animal;
    }

    public Player player() {
        return player;
    }

    public ItemStack result() {
        return result;
    }

    /**
     * Not empty: cancel() gives nothing.
     */
    public void setResult(ItemStack result) {
        this.result = Stacks.notEmpty(result);
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
