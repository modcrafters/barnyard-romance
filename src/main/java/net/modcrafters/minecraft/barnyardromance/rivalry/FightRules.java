package net.modcrafters.minecraft.barnyardromance.rivalry;

import net.modcrafters.minecraft.barnyardromance.config.Tuning;

import java.util.UUID;

// How rival males fight: they clash heads every CLASH_TICKS, a bigger male hitting harder, until one drops. Food in a
// player's hand only stops a fight between two males that both know people.
public final class FightRules {
    public static final int CLASH_TICKS = 40;
    private static final double CLASH_DAMAGE = 2.0;

    private FightRules() {
    }

    public static float clashDamage(double scale) {
        return (float) (CLASH_DAMAGE * scale);
    }

    public static boolean stops(boolean aFamiliar, boolean bFamiliar, boolean foodNear) {
        return aFamiliar && bFamiliar && foodNear;
    }

    // Each male's own time of day (ticks after midnight) for his daily roll, so a herd doesn't roll all at once.
    public static int rollTime(UUID id) {
        return (int) Math.floorMod(id.getLeastSignificantBits(), 24_000L);
    }

    // Roosters may fight whether wild or not; other males only when both are wild.
    public static double randomChance(boolean rooster, boolean aWild, boolean bWild, Tuning tuning) {
        if (rooster) {
            return tuning.roosterFightChance();
        }
        return aWild && bWild ? tuning.wildFightChance() : 0.0;
    }

    public static boolean due(long rollDay, long today, int sinceMidnight, int rollTime) {
        return rollDay < today && sinceMidnight >= rollTime;
    }
}
