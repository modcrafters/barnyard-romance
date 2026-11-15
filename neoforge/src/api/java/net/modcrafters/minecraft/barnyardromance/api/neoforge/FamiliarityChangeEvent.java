package net.modcrafters.minecraft.barnyardromance.api.neoforge;

import net.modcrafters.minecraft.barnyardromance.api.event.FamiliarityChange;

/**
 * NeoForge's side of FamiliarityChange: an animal's familiarity is about to change. See FamiliarityChange for what it carries and what a change or cancel does.
 */
public final class FamiliarityChangeEvent extends CancellableBarnyardNeoForgeEvent<FamiliarityChange> {
    public FamiliarityChangeEvent(FamiliarityChange apiEvent) {
        super(apiEvent);
    }
}
