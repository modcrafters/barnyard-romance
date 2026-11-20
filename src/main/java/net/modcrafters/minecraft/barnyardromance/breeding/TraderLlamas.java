package net.modcrafters.minecraft.barnyardromance.breeding;

// A wandering trader's llamas are bred for the road: big, and strong enough to carry a lot.
public final class TraderLlamas {
    public static final String SPECIES = "minecraft:trader_llama";

    private TraderLlamas() {
    }

    // 3, 4 or 5 with equal odds. Vanilla's own roll is 1 to 3, and 5 only one time in 25.
    public static int strength(double unitRandom) {
        return Math.min(5, 3 + (int) (Math.clamp(unitRandom, 0.0, 1.0) * 3.0));
    }
}
