package net.modcrafters.minecraft.barnyardromance.core.config;

import org.jspecify.annotations.Nullable;

// One way to milk a species: holding the item bucket gives the item result (both item ids), with the sound id if
// there is one.
public record MilkEntry(String bucket, String result, @Nullable String sound) {
}
