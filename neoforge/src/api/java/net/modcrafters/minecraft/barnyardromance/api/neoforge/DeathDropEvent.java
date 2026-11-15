package net.modcrafters.minecraft.barnyardromance.api.neoforge;

import net.modcrafters.minecraft.barnyardromance.api.event.DeathDrop;

/**
 * NeoForge's side of DeathDrop: one stack a tracked animal drops when it dies. See DeathDrop for what it carries and what a change or cancel does.
 */
public final class DeathDropEvent extends CancellableBarnyardNeoForgeEvent<DeathDrop> {
    public DeathDropEvent(DeathDrop apiEvent) {
        super(apiEvent);
    }
}
