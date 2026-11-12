package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.familiarity.api.Upkeep;

// Familiarity's slice of the animal record (see FamiliaritySlices.CARE). familiarity runs 0 to 1; lastFedTick is the
// overworld clock time of the last hand feeding, -1 for never. origin says whether the animal was raised from young or
// won over as an adult (see WildCaught); upkeep what the herd's upkeep has done to it.
public record FamiliarityState(double familiarity, long lastFedTick, Hunger hunger, Origin origin, Refusals refusals, Upkeep upkeep) {
    public static final FamiliarityState NONE = new FamiliarityState(0.0, -1L, Hunger.NONE, Origin.UNKNOWN, Refusals.NONE, Upkeep.NONE);

    public FamiliarityState fed(double familiarity, long lastFedTick) {
        return new FamiliarityState(familiarity, lastFedTick, hunger, origin, refusals, upkeep);
    }

    public FamiliarityState withFamiliarity(double familiarity) {
        return new FamiliarityState(familiarity, lastFedTick, hunger, origin, refusals, upkeep);
    }

    public FamiliarityState withHunger(Hunger hunger) {
        return new FamiliarityState(familiarity, lastFedTick, hunger, origin, refusals, upkeep);
    }

    public FamiliarityState withOrigin(Origin origin) {
        return new FamiliarityState(familiarity, lastFedTick, hunger, origin, refusals, upkeep);
    }

    public FamiliarityState withRefusals(Refusals refusals) {
        return new FamiliarityState(familiarity, lastFedTick, hunger, origin, refusals, upkeep);
    }

    public FamiliarityState withUpkeep(Upkeep upkeep) {
        return new FamiliarityState(familiarity, lastFedTick, hunger, origin, refusals, upkeep);
    }
}
