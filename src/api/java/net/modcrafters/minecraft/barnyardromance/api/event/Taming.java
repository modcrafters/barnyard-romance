package net.modcrafters.minecraft.barnyardromance.api.event;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.modcrafters.minecraft.barnyardromance.api.Cancellable;

/**
 * Familiarity is about to tame an animal: a horse, wolf, cat or Naturalist pet becomes the player's (TAME), or an
 * ocelot comes to trust them (TRUST). Cancelled, the animal stays as it is and tries again at its next meal.
 */
public final class Taming implements Cancellable {
    /** TAME: the animal becomes the player's. TRUST: an ocelot comes to trust them, as its fish would. */
    public enum Kind {
        TAME,
        TRUST
    }

    private final Animal animal;
    private final Player player;
    private final Kind kind;
    private boolean cancelled;

    public Taming(Animal animal, Player player, Kind kind) {
        this.animal = animal;
        this.player = player;
        this.kind = kind;
    }

    public Animal animal() {
        return animal;
    }

    public Player player() {
        return player;
    }

    public Kind kind() {
        return kind;
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
