package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.age.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.age.Aging;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;

// Each birth wears a mother out a little. Fertility is stored with the tick it was set and recovers with the clock,
// so nothing has to tick and the client can show it without a new sync.
public final class Fertility {
    public static final long DAY_TICKS = 24_000L;

    private Fertility() {
    }

    public static double current(double fertility, long since, long now) {
        return current(fertility, since, now, Long.MAX_VALUE);
    }

    // Ticks from oldFrom on recover at oldFertilityFactor of the usual rate.
    public static double current(double fertility, long since, long now, long oldFrom) {
        if (since < 0L || now <= since) {
            return Math.clamp(fertility, 0.0, 1.0);
        }
        long young = Math.max(0L, Math.min(now, oldFrom) - since);
        long old = Math.max(0L, now - Math.max(since, oldFrom));
        var tuning = BarnyardConfig.current().tuning();
        double recovered = tuning.fertilityRecoveryPerDay() * (young + old * tuning.oldFertilityFactor()) / DAY_TICKS;
        return Math.clamp(fertility + recovered, 0.0, 1.0);
    }

    public static double current(AnimalTraits traits, AgeSettings age, long now) {
        return current(traits.fertility(), traits.fertilityTick(), now, Aging.oldFrom(traits.bornTick(), age));
    }

    public static double afterBirth(double current) {
        return Math.max(0.0, current - BarnyardConfig.current().tuning().fertilityCostPerBirth());
    }

    public static boolean canConceive(double current) {
        return current >= BarnyardConfig.current().tuning().fertilityMinToConceive();
    }

    public static boolean wornOut(AnimalTraits traits, AgeSettings age, long now) {
        return !canConceive(current(traits, age, now));
    }
}
