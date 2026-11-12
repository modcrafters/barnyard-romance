package net.modcrafters.minecraft.barnyardromance.breeding.api;

// What a newborn inherits from one parent: its size, how fast it grew up and its weight.
public record Parent(double scale, int growthTicks, double weight) {
}
