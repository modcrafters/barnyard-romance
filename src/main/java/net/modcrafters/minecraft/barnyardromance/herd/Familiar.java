package net.modcrafters.minecraft.barnyardromance.herd;

// Whether the herd book keeps an animal up to date: below the familiarity threshold it stops,
// and says "not familiar" or, for one that was familiar while in the book, "no longer familiar".
public enum Familiar {
    FAMILIAR,
    NOT_YET,
    NO_LONGER
}
