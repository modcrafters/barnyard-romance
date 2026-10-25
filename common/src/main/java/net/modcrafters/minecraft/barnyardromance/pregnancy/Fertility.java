package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;

// Each birth wears a mother out a little. Fertility is stored with the tick it was set and recovers with the clock,
// so nothing has to tick and the client can show it without a new sync.
public final class Fertility {
    public static final long DAY_TICKS = 24_000L;

    private Fertility() {
    }

    public static double current(double fertility, long since, long now) {
        if (since < 0L || now <= since) {
            return Math.clamp(fertility, 0.0, 1.0);
        }
        double recovered = BarnyardConfig.current().tuning().fertilityRecoveryPerDay() * (now - since) / DAY_TICKS;
        return Math.clamp(fertility + recovered, 0.0, 1.0);
    }

    public static double current(AnimalTraits traits, long now) {
        return current(traits.fertility(), traits.fertilityTick(), now);
    }

    public static double afterBirth(double current) {
        return Math.max(0.0, current - BarnyardConfig.current().tuning().fertilityCostPerBirth());
    }

    public static boolean canConceive(double current) {
        return current >= BarnyardConfig.current().tuning().fertilityMinToConceive();
    }

    public static boolean wornOut(AnimalTraits traits, long now) {
        return !canConceive(current(traits, now));
    }
}
