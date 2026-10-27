package net.modcrafters.minecraft.barnyardromance.look;

public final class HornRoll {
    // Mixed in so horns and the death spread (Aging.spreadRandom) do not follow each other.
    private static final long SALT = 0x5DEECE66DL;

    private HornRoll() {
    }

    // Whether a female has horns, from her UUID: the same answer for her whole life, and no storage.
    public static boolean femaleHorned(long uuidMost, long uuidLeast, double chance) {
        long mixed = (uuidMost ^ Long.rotateLeft(uuidLeast, 17) ^ SALT) * 0x9E3779B97F4A7C15L;
        mixed ^= mixed >>> 29;
        mixed *= 0xBF58476D1CE4E5B9L;
        mixed ^= mixed >>> 32;
        return (mixed >>> 11) * 0x1.0p-53 < chance;
    }
}
