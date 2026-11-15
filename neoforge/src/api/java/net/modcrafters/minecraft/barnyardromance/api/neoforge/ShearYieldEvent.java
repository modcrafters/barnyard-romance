package net.modcrafters.minecraft.barnyardromance.api.neoforge;

import net.modcrafters.minecraft.barnyardromance.api.event.ShearYield;

// NeoForge's side of ShearYield: one stack of wool from a shearing. See ShearYield for what it carries and what a change or cancel does.
public final class ShearYieldEvent extends CancellableBarnyardNeoForgeEvent<ShearYield> {
    public ShearYieldEvent(ShearYield apiEvent) {
        super(apiEvent);
    }
}
