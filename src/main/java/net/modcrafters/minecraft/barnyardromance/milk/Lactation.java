package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;

// A female's milk, saved with her traits. milkUntil and youngUntil are game ticks, milkedDay a GameDay, -1 when unset.
// Milk flows until milkUntil, and while her youngest baby is still a baby plus the keep-flowing time after that.
// The daily milking count resets at midnight on the overworld clock.
public record Lactation(long milkUntil, long youngUntil, long milkedDay, int milkedToday, boolean graceGiven) {
    public static final Lactation NONE = new Lactation(-1L, -1L, -1L, 0, false);

    public long endsAt(long keepFlowingTicks) {
        return Math.max(milkUntil, youngUntil < 0L ? -1L : youngUntil + keepFlowingTicks);
    }

    public boolean hasMilk(long now, long keepFlowingTicks) {
        return now < endsAt(keepFlowingTicks);
    }

    public boolean nursing(long now) {
        return now < youngUntil;
    }

    public int milkingsLeft(long clock, int perDay) {
        if (milkedDay != GameDay.of(clock)) {
            return perDay;
        }
        return Math.max(0, perDay - milkedToday);
    }

    public Lactation milked(long now, long clock, long keepFlowingTicks) {
        long today = GameDay.of(clock);
        return new Lactation(
                Math.max(milkUntil, now + keepFlowingTicks),
                youngUntil,
                today,
                milkedDay == today ? milkedToday + 1 : 1,
                graceGiven
        );
    }

    public Lactation afterBirth(long now, long youngUntil, long lactationTicks) {
        return new Lactation(
                Math.max(milkUntil, now + lactationTicks),
                Math.max(this.youngUntil, youngUntil),
                milkedDay,
                milkedToday,
                true
        );
    }

    // The one milk window an animal with no known birth gets, so dairy farms from before Stage 5 keep working.
    public Lactation grace(long now, long keepFlowingTicks) {
        long until = graceGiven ? milkUntil : Math.max(milkUntil, now + keepFlowingTicks);
        return new Lactation(until, youngUntil, milkedDay, milkedToday, true);
    }

    public Lactation withGraceGiven() {
        return new Lactation(milkUntil, youngUntil, milkedDay, milkedToday, true);
    }
}
