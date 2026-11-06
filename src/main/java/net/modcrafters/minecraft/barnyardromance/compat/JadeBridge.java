package net.modcrafters.minecraft.barnyardromance.compat;

// Set by the Jade plugin when Jade registers it, so our own card stays out of Jade's way. No Jade imports here:
// this class loads with or without Jade.
public final class JadeBridge {
    private static volatile boolean active;

    private JadeBridge() {
    }

    public static void markActive() {
        active = true;
    }

    public static boolean active() {
        return active;
    }
}
