package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public final class MatingGate {
    private MatingGate() {
    }

    public static boolean allows(Animal self, Animal partner) {
        try {
            Optional<AnimalTraits> selfTraits = Services.DATA.get(self);
            Optional<AnimalTraits> partnerTraits = Services.DATA.get(partner);
            return MatingRules.canMate(
                    selfTraits.map(AnimalTraits::sex),
                    partnerTraits.map(AnimalTraits::sex),
                    Services.DATA.getPregnancy(self).isPresent(),
                    Services.DATA.getPregnancy(partner).isPresent(),
                    selfTraits.map(traits -> Familiarity.ready(traits.familiarity())).orElse(true),
                    partnerTraits.map(traits -> Familiarity.ready(traits.familiarity())).orElse(true)
            );
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to check mating for {}, leaving vanilla rules in place", AnimalSex.speciesId(self), exception);
            return true;
        }
    }
}
