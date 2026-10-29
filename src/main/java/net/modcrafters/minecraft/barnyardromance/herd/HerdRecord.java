package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import org.jspecify.annotations.Nullable;

// What the server last saw of an animal in a herd book, so the book can show it while it isn't loaded.
// The pregnancy is kept without its litter; diedAt (game time) and diedClock (overworld clock) are -1 while the
// animal is alive, and diedClock is also -1 for deaths recorded before it was kept.
public record HerdRecord(
        String species,
        String name,
        AnimalTraits traits,
        @Nullable Pregnancy pregnancy,
        boolean baby,
        boolean milkable,
        // A sheep, and whether it has its wool; woolly is false from shearing until it eats grass.
        boolean shearable,
        boolean woolly,
        long refreshedAt,
        long diedAt,
        long diedClock
) {
    public boolean alive() {
        return diedAt < 0L;
    }

    public HerdRecord died(long tick, long clock) {
        return new HerdRecord(species, name, traits, pregnancy, baby, milkable, shearable, woolly, refreshedAt, tick, clock);
    }
}
