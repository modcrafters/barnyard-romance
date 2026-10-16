package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.sex.Sex;

import java.util.Optional;

public final class MatingRules {
    private MatingRules() {
    }

    public static boolean canMate(Optional<Sex> self, Optional<Sex> partner, boolean selfPregnant, boolean partnerPregnant) {
        if (self.isEmpty() || partner.isEmpty()) {
            return true;
        }
        if (self.get() == partner.get()) {
            return false;
        }
        if (self.get() == Sex.FEMALE && selfPregnant) {
            return false;
        }
        if (partner.get() == Sex.FEMALE && partnerPregnant) {
            return false;
        }
        return true;
    }
}
