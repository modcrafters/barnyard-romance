package net.modcrafters.minecraft.barnyardromance.api.forge;

import net.modcrafters.minecraft.barnyardromance.api.event.Befriended;

/**
 * Forge's side of Befriended: an animal became familiar to a player. See Befriended for what it carries and what a change or cancel does.
 */
public final class BefriendedEvent extends BarnyardForgeEvent<Befriended> {
    public BefriendedEvent(Befriended apiEvent) {
        super(apiEvent);
    }
}
