package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvents;
import net.modcrafters.minecraft.barnyardromance.api.event.FamiliarityChange;

// Every familiarity write on a loaded animal asks other mods first (the public FamiliarityChange event).
public final class FamiliarityChanges {
    private FamiliarityChanges() {
    }

    // The familiarity to write instead of after. Nothing is asked when nothing changes, or on the client, which only
    // mirrors what the server decided. cap is the animal's wild-caught cap.
    public static double decide(Animal animal, double before, double after, FamiliarityChange.Reason reason, double cap) {
        if (before == after || animal.level().isClientSide()) {
            return after;
        }
        return outcome(BarnyardEvents.FAMILIARITY_CHANGE.post(new FamiliarityChange(animal, before, after, reason)), after, cap);
    }

    // What a posted change comes to: before when cancelled or set to NaN; after when no listener changed it, even past
    // the cap, which an animal can be from before caps; otherwise the listener's value, kept within 0 and the cap.
    static double outcome(FamiliarityChange posted, double after, double cap) {
        if (posted.isCancelled() || Double.isNaN(posted.after())) {
            return posted.before();
        }
        if (posted.after() == after) {
            return after;
        }
        return Math.clamp(posted.after(), 0.0, Math.max(0.0, cap));
    }
}
