package net.modcrafters.minecraft.barnyardromance.api.neoforge;

import net.modcrafters.minecraft.barnyardromance.api.event.Born;

/**
 * NeoForge's side of Born: a mother gave birth. See Born for what it carries and what a change or cancel does.
 */
public final class BornEvent extends BarnyardNeoForgeEvent<Born> {
    public BornEvent(Born apiEvent) {
        super(apiEvent);
    }
}
