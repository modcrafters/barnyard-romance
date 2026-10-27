package net.modcrafters.minecraft.barnyardromance.look;

// A point, size or rotation in model pixels or degrees.
public record Xyz(float x, float y, float z) {
    public static final Xyz ZERO = new Xyz(0.0f, 0.0f, 0.0f);
}
