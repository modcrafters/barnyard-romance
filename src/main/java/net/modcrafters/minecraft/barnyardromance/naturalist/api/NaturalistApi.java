package net.modcrafters.minecraft.barnyardromance.naturalist.api;

// What other features ask of the Naturalist compat.
public interface NaturalistApi {
    // Whether the species is one of Naturalist's livestock, which follow the barnyard's rules.
    boolean isLivestock(String speciesId);
}
