package net.modcrafters.minecraft.barnyardromance.api;

/**
 * A pregnancy: how long until the birth, how far along (0 to 1), and the father's species id, e.g. "minecraft:cow".
 */
public record PregnancyInfo(long ticksUntilDue, double progress, String fatherSpecies) {
}
