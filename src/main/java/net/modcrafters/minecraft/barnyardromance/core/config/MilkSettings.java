package net.modcrafters.minecraft.barnyardromance.core.config;

import java.util.List;

// A species' milking entries. fromConfig is true when a species entry's own list replaced the built-in one.
public record MilkSettings(List<MilkEntry> entries, boolean fromConfig) {
    public static final MilkSettings NONE = new MilkSettings(List.of(), false);
}
