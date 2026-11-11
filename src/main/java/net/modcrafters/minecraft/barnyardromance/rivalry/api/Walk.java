package net.modcrafters.minecraft.barnyardromance.rivalry.api;

// What a path search says about walking somewhere: it can, it can't (the path goes some way and stops short), or
// nothing, when the search never left the block it started in. That happens to an animal standing in some spots of a
// pen's corner, between two fence lines, though it can walk out of it.
public enum Walk {
    YES,
    NO,
    UNKNOWN
}
