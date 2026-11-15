package net.modcrafters.minecraft.barnyardromance.api.neoforge;

import net.modcrafters.minecraft.barnyardromance.api.event.Taming;

// NeoForge's side of Taming: familiarity is about to tame an animal. See Taming for what it carries and what a change or cancel does.
public final class TamingEvent extends CancellableBarnyardNeoForgeEvent<Taming> {
    public TamingEvent(Taming apiEvent) {
        super(apiEvent);
    }
}
