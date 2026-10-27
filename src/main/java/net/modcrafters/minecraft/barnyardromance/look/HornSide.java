package net.modcrafters.minecraft.barnyardromance.look;

// LEFT is +X, the animal's own left. A goat that lost a horn skips that side.
public enum HornSide {
    ANY,
    LEFT,
    RIGHT;

    public HornSide mirrored() {
        return switch (this) {
            case ANY -> ANY;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
