package net.modcrafters.minecraft.barnyardromance.api.forge;

import net.modcrafters.minecraft.barnyardromance.api.event.FamiliarityChange;

/**
 * Forge's side of FamiliarityChange: an animal's familiarity is about to change. See FamiliarityChange for what it carries and what a change or cancel does.
 */
public final class FamiliarityChangeEvent extends CancellableBarnyardForgeEvent<FamiliarityChange> {
    public FamiliarityChangeEvent(FamiliarityChange apiEvent) {
        super(apiEvent);
    }
}
