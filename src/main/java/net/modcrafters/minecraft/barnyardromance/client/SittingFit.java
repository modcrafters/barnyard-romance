package net.modcrafters.minecraft.barnyardromance.client;

// How a hen is drawn in a nesting box: how far her legs sink into the hay, and how much she's shrunk to fit under the
// roof. Most birds are about as tall as their hitbox, and their legs are a share of that.
record SittingFit(double sinkBy, float scale) {
    // The hollow above the hay is ten pixels tall; a hen taller than most of it is drawn smaller.
    private static final double ROOM = 9.0 / 16.0;
    // A chicken's legs are this share of its height; they go in the hay.
    private static final double LEGS = 0.4;
    // Naturalist's turkey is drawn far taller than her hitbox: her tail fan stands well above her back. In blocks, at
    // full size.
    private static final double TURKEY_TALL = 24.5 / 16.0;
    private static final double TURKEY_LEGS = 5.0 / 16.0;

    // henScale is the size she's drawn at outside the box, which hitboxHeight already includes.
    static SittingFit of(String species, double hitboxHeight, double henScale) {
        boolean turkey = "naturalist:turkey".equals(species);
        double height = turkey ? TURKEY_TALL * henScale : hitboxHeight;
        double sink = turkey ? TURKEY_LEGS * henScale : height * LEGS;
        double above = height - sink;
        return new SittingFit(sink, above > ROOM ? (float) (ROOM / above) : 1.0F);
    }
}
