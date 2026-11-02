package net.modcrafters.minecraft.barnyardromance.familiarity;

// Where an animal's familiarity started: set the first time it gains any, and never changed after. An animal won
// over as an adult stays a little wild (see WildCaught); one raised from young can become fully familiar.
public enum Origin {
    UNKNOWN,
    FARM,
    WILD
}
