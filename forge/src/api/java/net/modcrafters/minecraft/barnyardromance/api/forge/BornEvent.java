package net.modcrafters.minecraft.barnyardromance.api.forge;

import net.modcrafters.minecraft.barnyardromance.api.event.Born;

/**
 * Forge's side of Born: a mother gave birth. See Born for what it carries and what a change or cancel does.
 */
public final class BornEvent extends BarnyardForgeEvent<Born> {
    public BornEvent(Born apiEvent) {
        super(apiEvent);
    }
}
