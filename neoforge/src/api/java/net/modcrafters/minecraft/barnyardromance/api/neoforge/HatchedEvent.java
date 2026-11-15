package net.modcrafters.minecraft.barnyardromance.api.neoforge;

import net.modcrafters.minecraft.barnyardromance.api.event.Hatched;

// NeoForge's side of Hatched: a chick hatched. See Hatched for what it carries and what a change or cancel does.
public final class HatchedEvent extends BarnyardNeoForgeEvent<Hatched> {
    public HatchedEvent(Hatched apiEvent) {
        super(apiEvent);
    }
}
