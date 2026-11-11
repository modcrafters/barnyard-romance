package net.modcrafters.minecraft.barnyardromance.stats;

import net.modcrafters.minecraft.barnyardromance.core.text.Text;

// fill is how full the line's bar is, from 0 to 1, for familiarity, growth, weight, health, fertility, vigour and
// pregnancy; 0 for the other kinds.
public record StatLine(StatKind kind, Text text, double fill) {
    static StatLine of(StatKind kind, Text text) {
        return new StatLine(kind, text, 0.0);
    }
}
