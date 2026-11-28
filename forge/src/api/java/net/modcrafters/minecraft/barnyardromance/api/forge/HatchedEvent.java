package net.modcrafters.minecraft.barnyardromance.api.forge;

import net.modcrafters.minecraft.barnyardromance.api.event.Hatched;

/**
 * Forge's side of Hatched: a chick hatched. See Hatched for what it carries and what a change or cancel does.
 */
public final class HatchedEvent extends BarnyardForgeEvent<Hatched> {
    public HatchedEvent(Hatched apiEvent) {
        super(apiEvent);
    }
}
