package net.modcrafters.minecraft.barnyardromance.api.forge;

import net.modcrafters.minecraft.barnyardromance.api.event.Taming;

/**
 * Forge's side of Taming: familiarity is about to tame an animal. See Taming for what it carries and what a change or cancel does.
 */
public final class TamingEvent extends CancellableBarnyardForgeEvent<Taming> {
    public TamingEvent(Taming apiEvent) {
        super(apiEvent);
    }
}
