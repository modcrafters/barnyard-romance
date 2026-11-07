package net.modcrafters.minecraft.barnyardromance.vigour;

// How a male's Vigour fills and is spent. A male fed on a day starts the next one full; one who missed yesterday is
// filled by his first meal today; otherwise what he has left carries over. Matings and fertilized eggs spend it, and a
// hungry male (fed neither yesterday nor today) also pays with health, and won't mate at all if it would leave him
// under 1: nobody kills himself for love.
public final class VigourRules {
    private static final double EPSILON = 1.0e-9;

    private VigourRules() {
    }

    // Settling always runs before a meal is recorded, so a meal on any day from v.day up to yesterday was followed by
    // a midnight that filled him, and nothing has been spent since.
    // It runs every tick, so a day already settled hands back the same Vigour instead of making a new one.
    public static Vigour settle(Vigour vigour, long lastAteDay, long today) {
        if (vigour.day() >= today) {
            return vigour;
        }
        if (lastAteDay >= vigour.day() && lastAteDay < today) {
            return new Vigour(1.0, today, vigour.rollDay());
        }
        return new Vigour(vigour.value(), today, vigour.rollDay());
    }

    // lastAteDayBefore is his last meal day before this one.
    public static Vigour onMeal(Vigour settled, long lastAteDayBefore, long today) {
        return hungry(lastAteDayBefore, today) ? settled.withValue(1.0) : settled;
    }

    // Never fed (-1) is hungry, even on day 0, where -1 would otherwise pass for yesterday.
    public static boolean hungry(long lastAteDay, long today) {
        return lastAteDay < 0L || (lastAteDay != today && lastAteDay != today - 1L);
    }

    public static boolean canPay(double vigour, double cost, boolean hungry, double health, double healthCost) {
        if (vigour < cost - EPSILON) {
            return false;
        }
        return !hungry || health - healthCost >= 1.0 - EPSILON;
    }

    public static Vigour pay(Vigour vigour, double cost) {
        return vigour.withValue(Math.max(0.0, vigour.value() - cost));
    }

    public static Vigour add(Vigour vigour, double amount) {
        return vigour.withValue(Math.min(1.0, vigour.value() + Math.max(0.0, amount)));
    }
}
