package net.modcrafters.minecraft.barnyardromance.herd;

// Whether the herd book shows a sheep as ready to shear. NONE is for animals that aren't sheared at all, and lambs.
public enum ShearState {
    NONE,
    READY,
    SHEARED_TODAY,
    // Sheared on an earlier day, and hasn't eaten grass to grow its wool back yet.
    REGROWING
}
