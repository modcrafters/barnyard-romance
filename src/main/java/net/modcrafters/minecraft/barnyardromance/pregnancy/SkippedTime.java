package net.modcrafters.minecraft.barnyardromance.pregnancy;

// The game time and overworld clock time a pregnancy was last looked at. Sleeping or /time add moves the clock
// without the game time a pregnancy counts in, so the clock getting ahead of the game time is time skipped.
public record SkippedTime(long game, long clock) {
    public static final SkippedTime UNSEEN = new SkippedTime(Long.MIN_VALUE, Long.MIN_VALUE);

    public boolean seen() {
        return game != Long.MIN_VALUE;
    }

    // How far the clock got ahead of the game time since then: positive for time skipped, negative while the
    // daylight cycle is stopped or after the clock was set back.
    public long drift(long game, long clock) {
        return seen() ? (clock - this.clock) - (game - this.game) : 0L;
    }
}
