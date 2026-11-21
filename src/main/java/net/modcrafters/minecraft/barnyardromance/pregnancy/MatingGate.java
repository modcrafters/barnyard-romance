package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.animal.Tracked;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.api.Sex;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.rivalry.api.RivalryApi;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public final class MatingGate {
    private MatingGate() {
    }

    // Vanilla's rules unless both are tracked: an animal whose species was switched off keeps what the mod saved on it,
    // sex and pregnancy included, but none of it stops it mating.
    public static boolean allows(Animal self, Animal partner) {
        try {
            Optional<AnimalTraits> selfTraits = Tracked.traits(self);
            Optional<AnimalTraits> partnerTraits = Tracked.traits(partner);
            if (selfTraits.isEmpty() || partnerTraits.isEmpty()) {
                return true;
            }
            // A tracked couple mates only as Courtship paired them.
            if (selfTraits.get().sex() != partnerTraits.get().sex()
                    && !(selfTraits.get().sex() == Sex.FEMALE
                    ? FeaturesRepository.get(RivalryApi.class).chosen(self, partner)
                    : FeaturesRepository.get(RivalryApi.class).chosen(partner, self))) {
                return false;
            }
            return MatingRules.canMate(
                    selfTraits.map(AnimalTraits::sex),
                    partnerTraits.map(AnimalTraits::sex),
                    Services.DATA.get(self, PregnancyFeature.PREGNANCY).isPresent(),
                    Services.DATA.get(partner, PregnancyFeature.PREGNANCY).isPresent(),
                    selfTraits.map(FeaturesRepository.get(FamiliarityApi.class)::isFamiliar).orElse(true),
                    partnerTraits.map(FeaturesRepository.get(FamiliarityApi.class)::isFamiliar).orElse(true)
            );
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to check mating for {}, leaving vanilla rules in place", AnimalSex.speciesId(self), exception);
            return true;
        }
    }
}
