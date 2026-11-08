package net.modcrafters.minecraft.barnyardromance.feed;

// Whether a feeding block has ticked without a break, that is without being unloaded, since it last asked. It ticks
// this every tick, also while it waits for the animals around it to load, so a long wait at midnight isn't taken for a
// break; a break during the wait is kept until it asks.
final class TickGap {
    // Ticks further apart than this are a break.
    private static final long RUNNING_TICKS = 2L;

    private long lastTick = Long.MIN_VALUE;
    private boolean broke;

    void tick(long now) {
        if (lastTick < now - RUNNING_TICKS) {
            broke = true;
        }
        lastTick = now;
    }

    // Ran on since it last asked; asking starts over.
    boolean ranOn() {
        boolean ranOn = !broke;
        broke = false;
        return ranOn;
    }
}
