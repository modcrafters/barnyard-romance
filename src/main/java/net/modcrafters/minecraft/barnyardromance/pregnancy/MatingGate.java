package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.rivalry.Courtship;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public final class MatingGate {
    private MatingGate() {
    }

    public static boolean allows(Animal self, Animal partner) {
        try {
            Optional<AnimalTraits> selfTraits = Services.DATA.get(self);
            Optional<AnimalTraits> partnerTraits = Services.DATA.get(partner);
            // A tracked couple mates only as Courtship paired them.
            if (selfTraits.isPresent() && partnerTraits.isPresent() && selfTraits.get().sex() != partnerTraits.get().sex()
                    && !(selfTraits.get().sex() == Sex.FEMALE ? Courtship.chosen(self, partner) : Courtship.chosen(partner, self))) {
                return false;
            }
            return MatingRules.canMate(
                    selfTraits.map(AnimalTraits::sex),
                    partnerTraits.map(AnimalTraits::sex),
                    Services.DATA.get(self, PregnancyFeature.PREGNANCY).isPresent(),
                    Services.DATA.get(partner, PregnancyFeature.PREGNANCY).isPresent(),
                    selfTraits.map(traits -> Familiarity.ready(traits.get(FamiliaritySlices.CARE).familiarity())).orElse(true),
                    partnerTraits.map(traits -> Familiarity.ready(traits.get(FamiliaritySlices.CARE).familiarity())).orElse(true)
            );
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to check mating for {}, leaving vanilla rules in place", AnimalSex.speciesId(self), exception);
            return true;
        }
    }
}
