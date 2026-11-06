package net.modcrafters.minecraft.barnyardromance.rivalry;

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
}
