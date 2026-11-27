package net.modcrafters.minecraft.barnyardromance.api.forge;

import net.modcrafters.minecraft.barnyardromance.api.event.ShearYield;

/**
 * Forge's side of ShearYield: one stack of wool from a shearing. See ShearYield for what it carries and what a change or cancel does.
 */
public final class ShearYieldEvent extends CancellableBarnyardForgeEvent<ShearYield> {
    public ShearYieldEvent(ShearYield apiEvent) {
        super(apiEvent);
    }
}
