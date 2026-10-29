package net.modcrafters.minecraft.barnyardromance.shear;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.herd.ShearState;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;

// A sheep can be sheared once a day, like milking, and still needs to eat grass to grow its wool back as in
// vanilla. The day turns over at midnight on the overworld clock (GameDay).
public final class ShearRule {
    private ShearRule() {
    }

    public static boolean shearedToday(AnimalTraits traits, long clock) {
        return traits.lastShearedClock() >= 0L && GameDay.sameDay(traits.lastShearedClock(), clock);
    }

    // woolly is false once a sheep is sheared, until it eats grass.
    public static ShearState state(boolean shearable, boolean woolly, boolean baby, AnimalTraits traits, long clock) {
        if (!shearable || baby) {
            return ShearState.NONE;
        }
        if (shearedToday(traits, clock)) {
            return ShearState.SHEARED_TODAY;
        }
        return woolly ? ShearState.READY : ShearState.REGROWING;
    }
}
