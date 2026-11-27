package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvents;
import net.modcrafters.minecraft.barnyardromance.api.event.FamiliarityChange;
import net.modcrafters.minecraft.barnyardromance.core.compat.Jdk;

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
    // the cap, which an animal can be from before caps or a lowered cap; otherwise the listener's value, kept at 0 or
    // above and no higher than the cap, or than where the animal already is when that's higher. So a listener can
    // hold or slow an animal above its cap, but never push it further.
    static double outcome(FamiliarityChange posted, double after, double cap) {
        if (posted.isCancelled() || Double.isNaN(posted.after())) {
            return posted.before();
        }
        if (posted.after() == after) {
            return after;
        }
        return Jdk.clamp(posted.after(), 0.0, Math.max(Math.max(0.0, cap), posted.before()));
    }
}
