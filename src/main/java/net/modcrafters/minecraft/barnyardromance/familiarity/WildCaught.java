package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;

// An animal first won over as an adult never becomes fully familiar: livestock top out at wildFamiliarityCap, and
// pets (wolves, cats, the horse family), who live closer to you, at wildPetFamiliarityCap. Animals raised from
// young have no cap.
public final class WildCaught {
    private WildCaught() {
    }

    public static Origin decide(Origin current, boolean grown) {
        if (current != Origin.UNKNOWN) {
            return current;
        }
        return grown ? Origin.WILD : Origin.FARM;
    }

    public static double cap(Origin origin, boolean pet, Tuning tuning) {
        if (origin != Origin.WILD) {
            return 1.0;
        }
        return pet ? tuning.wildPetFamiliarityCap() : tuning.wildFamiliarityCap();
    }

    // Every familiarity gain goes through here: the first one decides the origin, and a wild animal is held at its
    // cap, even one that was above it before its origin was known.
    public static AnimalTraits raise(AnimalTraits traits, double familiarity, long lastFedTick, boolean grown, boolean pet, Tuning tuning) {
        Origin origin = familiarity > 0.0 ? decide(traits.origin(), grown) : traits.origin();
        return traits.fed(Math.min(familiarity, cap(origin, pet, tuning)), lastFedTick).withOrigin(origin);
    }

    public static double tamingGrace(double grace, Origin origin, boolean grown, Tuning tuning) {
        return Math.min(grace, cap(decide(origin, grown), true, tuning));
    }
}
