package net.modcrafters.minecraft.barnyardromance.stats;

// What a line of the stat sheet is about, so a page can give it an icon or a bar. NOTE and DETAIL lines get neither.
// A DETAIL explains the line above it: the herd book puts it in that line's tooltip, the card and Jade show it as a note.
// The order is the wire order of herd book lines, so new kinds go at the end.
public enum StatKind {
    SIZE,
    NOTE,
    FAMILIARITY,
    FED,
    NOT_FED,
    GROWTH,
    WEIGHT,
    HEALTH,
    FERTILITY,
    VIGOUR,
    MILK,
    SHEAR,
    EGGS,
    BREEDING,
    PREGNANT,
    DETAIL
}
