package net.modcrafters.minecraft.barnyardromance.api.forge;

import net.modcrafters.minecraft.barnyardromance.api.event.Milking;

/**
 * Forge's side of Milking: a player is about to milk an animal. See Milking for what it carries and what a change or cancel does.
 */
public final class MilkingEvent extends CancellableBarnyardForgeEvent<Milking> {
    public MilkingEvent(Milking apiEvent) {
        super(apiEvent);
    }
}
