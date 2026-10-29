package net.modcrafters.minecraft.barnyardromance.client;

// Gathers scrolling into whole page turns. A mouse wheel's notch is one turn; a touchpad sends many small scrolls,
// which add up to a turn instead of each turning a page. Scrolling the other way starts over.
// 1.21.1 has no ScrollWheelHandler, so this does the same here, and has no Minecraft types so it is unit tested.
public final class PageWheel {
    private double gathered;

    // How many pages to turn: positive to go back (scrolling up), negative to go forward.
    public int scroll(double amount) {
        if (amount == 0.0) {
            return 0;
        }
        if (Math.signum(amount) != Math.signum(this.gathered)) {
            this.gathered = 0.0;
        }
        this.gathered += amount;
        int turns = (int) this.gathered;
        this.gathered -= turns;
        return turns;
    }
}
