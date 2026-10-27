package net.modcrafters.minecraft.barnyardromance.look;

public final class HornSides {
    private HornSides() {
    }

    // Goats lose a horn when they ram something; the other animals always have both.
    public static boolean drawn(HornSide side, boolean hasLeft, boolean hasRight) {
        return switch (side) {
            case ANY -> true;
            case LEFT -> hasLeft;
            case RIGHT -> hasRight;
        };
    }
}
