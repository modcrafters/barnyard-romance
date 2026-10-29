package net.modcrafters.minecraft.barnyardromance.familiarity;

// Hungry days, saved with the traits. A hungry day is an overworld-clock day (GameDay, midnight to midnight) in which
// the animal ate nothing; each one costs familiarity. Days are settled lazily, like fertility: settledDay is the
// first day not yet accounted for, lastAteDay the day of the last meal and lastHungryDay the latest hungry day.
// -1 means never or unknown. Settling runs before every meal, so at most one unsettled day can have a meal in it.
public record Hunger(long settledDay, long lastAteDay, long lastHungryDay) {
    public static final Hunger NONE = new Hunger(-1L, -1L, -1L);

    // first is true for an animal settled for the first time (a new one, or one from before hunger was kept).
    public record Settled(Hunger hunger, int hungryDays, boolean first) {
    }

    public Hunger ate(long day) {
        return new Hunger(settledDay, day, lastHungryDay);
    }

    public boolean hungryYesterday(long today) {
        return lastHungryDay >= 0L && lastHungryDay == today - 1L;
    }

    // Counts the hungry days from settledDay up to yesterday. Nursing babies are fed by their mothers, so they
    // never go hungry. Nothing is counted the first time, so animals from older saves don't lose familiarity at once.
    public static Settled settle(Hunger hunger, long today, boolean nursing) {
        if (hunger.settledDay < 0L) {
            return new Settled(new Hunger(today, hunger.lastAteDay, hunger.lastHungryDay), 0, true);
        }
        if (today <= hunger.settledDay) {
            return new Settled(hunger, 0, false);
        }
        if (nursing) {
            return new Settled(new Hunger(today, hunger.lastAteDay, hunger.lastHungryDay), 0, false);
        }
        boolean ateInRange = hunger.lastAteDay >= hunger.settledDay && hunger.lastAteDay < today;
        long hungry = today - hunger.settledDay - (ateInRange ? 1L : 0L);
        long yesterday = today - 1L;
        long lastHungry = yesterday != hunger.lastAteDay ? yesterday
                : yesterday - 1L >= hunger.settledDay ? yesterday - 1L : hunger.lastHungryDay;
        return new Settled(new Hunger(today, hunger.lastAteDay, hungry > 0L ? lastHungry : hunger.lastHungryDay),
                (int) Math.min(hungry, Integer.MAX_VALUE), false);
    }

    public static double afterHunger(double familiarity, int hungryDays, double lossPerDay) {
        return Math.clamp(familiarity - hungryDays * lossPerDay, 0.0, 1.0);
    }
}
