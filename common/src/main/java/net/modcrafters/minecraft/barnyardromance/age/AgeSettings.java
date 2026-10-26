package net.modcrafters.minecraft.barnyardromance.age;

// How long a species stays young after growing up, and when it gets old and dies, counted from birth. 0 turns a step off.
public record AgeSettings(long youngTicks, long oldAfterTicks, long deathAfterTicks) {
    public static final AgeSettings DEFAULTS = new AgeSettings(24_000L, 720_000L, 1_440_000L);
}
