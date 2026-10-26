package net.modcrafters.minecraft.barnyardromance.milk;

// A female's milk, saved with her traits. Times are game ticks, -1 when unset.
// Milk flows until milkUntil, and while her youngest baby is still a baby plus the keep-flowing time after that.
public record Lactation(long milkUntil, long youngUntil, long windowStart, int milkedInWindow, boolean graceGiven) {
    public static final Lactation NONE = new Lactation(-1L, -1L, -1L, 0, false);
    public static final long WINDOW_TICKS = 24_000L;

    public long endsAt(long keepFlowingTicks) {
        return Math.max(milkUntil, youngUntil < 0L ? -1L : youngUntil + keepFlowingTicks);
    }

    public boolean hasMilk(long now, long keepFlowingTicks) {
        return now < endsAt(keepFlowingTicks);
    }

    public boolean nursing(long now) {
        return now < youngUntil;
    }

    public int milkingsLeft(long now, int perDay) {
        if (windowOver(now)) {
            return perDay;
        }
        return Math.max(0, perDay - milkedInWindow);
    }

    public Lactation milked(long now, long keepFlowingTicks) {
        boolean fresh = windowOver(now);
        return new Lactation(
                Math.max(milkUntil, now + keepFlowingTicks),
                youngUntil,
                fresh ? now : windowStart,
                fresh ? 1 : milkedInWindow + 1,
                graceGiven
        );
    }

    public Lactation afterBirth(long now, long youngUntil, long lactationTicks) {
        return new Lactation(
                Math.max(milkUntil, now + lactationTicks),
                Math.max(this.youngUntil, youngUntil),
                windowStart,
                milkedInWindow,
                true
        );
    }

    // The one milk window an animal with no known birth gets, so dairy farms from before Stage 5 keep working.
    public Lactation grace(long now, long keepFlowingTicks) {
        long until = graceGiven ? milkUntil : Math.max(milkUntil, now + keepFlowingTicks);
        return new Lactation(until, youngUntil, windowStart, milkedInWindow, true);
    }

    public Lactation withGraceGiven() {
        return new Lactation(milkUntil, youngUntil, windowStart, milkedInWindow, true);
    }

    private boolean windowOver(long now) {
        return windowStart < 0L || now >= windowStart + WINDOW_TICKS;
    }
}
