package net.modcrafters.minecraft.barnyardromance.familiarity;

// What the herd's upkeep has done to one animal. cared: it was looked after, so it is not neglected. ranAway: it
// ran off for want of care and wants a note in the herd book until it is cared for again. feederDay is the GameDay
// a feeder last fed it (-1 never) and feeder that feeder's kind id. inHerd: it counted as part of a herd the last
// time it was checked.
public record Upkeep(boolean cared, boolean ranAway, long feederDay, String feeder, boolean inHerd) {
    public static final Upkeep NONE = new Upkeep(false, false, -1L, "", false);
    // From a save before upkeep was kept. The first settle turns it into NONE or cared for (see FamiliarityCare).
    public static final Upkeep UNKNOWN = new Upkeep(false, false, -2L, "", false);

    public Upkeep caredFor() {
        return new Upkeep(true, false, feederDay, feeder, inHerd);
    }

    // Named runAway because ranAway() is the record's own accessor.
    public Upkeep runAway() {
        return new Upkeep(false, true, feederDay, feeder, inHerd);
    }

    public Upkeep fedBy(String feederKindId, long day) {
        return new Upkeep(cared, ranAway, day, feederKindId, inHerd);
    }

    public Upkeep withInHerd(boolean inHerd) {
        return new Upkeep(cared, ranAway, feederDay, feeder, inHerd);
    }

    public boolean fedByFeederOn(long day) {
        return feederDay == day;
    }
}
