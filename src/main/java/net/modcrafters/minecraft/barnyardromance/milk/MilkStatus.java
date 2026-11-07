package net.modcrafters.minecraft.barnyardromance.milk;

// What the herd book and the card show about a female's milk: still nursing, ticks until it dries up, and milkings left today.
public record MilkStatus(boolean nursing, long ticksLeft, int left, int perDay) {
}
