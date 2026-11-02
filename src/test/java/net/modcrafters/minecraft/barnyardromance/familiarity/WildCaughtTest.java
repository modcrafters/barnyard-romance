package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WildCaughtTest {
    private static final Tuning DEFAULTS = Tuning.DEFAULTS;

    @Test
    void decideKeepsAKnownOrigin() {
        assertEquals(Origin.FARM, WildCaught.decide(Origin.FARM, true));
        assertEquals(Origin.WILD, WildCaught.decide(Origin.WILD, false));
    }

    @Test
    void decideGoesByAgeWhenUnknown() {
        assertEquals(Origin.WILD, WildCaught.decide(Origin.UNKNOWN, true));
        assertEquals(Origin.FARM, WildCaught.decide(Origin.UNKNOWN, false));
    }

    @Test
    void onlyWildAnimalsAreCapped() {
        assertEquals(0.4, WildCaught.cap(Origin.WILD, false, DEFAULTS), 1.0e-9);
        assertEquals(0.8, WildCaught.cap(Origin.WILD, true, DEFAULTS), 1.0e-9);
        assertEquals(1.0, WildCaught.cap(Origin.FARM, false, DEFAULTS), 1.0e-9);
        assertEquals(1.0, WildCaught.cap(Origin.UNKNOWN, true, DEFAULTS), 1.0e-9);
    }

    @Test
    void aWildAdultStopsAtForty() {
        AnimalTraits raised = WildCaught.raise(traits(0.2), 0.6, 5L, true, false, DEFAULTS);

        assertEquals(0.4, raised.familiarity(), 1.0e-9);
        assertEquals(Origin.WILD, raised.origin());
        assertEquals(5L, raised.lastFedTick());
    }

    @Test
    void aYoungAnimalIsFarmRaisedAndUncapped() {
        AnimalTraits raised = WildCaught.raise(traits(0.0), 0.6, 5L, false, false, DEFAULTS);

        assertEquals(0.6, raised.familiarity(), 1.0e-9);
        assertEquals(Origin.FARM, raised.origin());
    }

    @Test
    void aFarmRaisedAdultIsUncapped() {
        AnimalTraits raised = WildCaught.raise(traits(0.8).withOrigin(Origin.FARM), 1.0, 5L, true, false, DEFAULTS);

        assertEquals(1.0, raised.familiarity(), 1.0e-9);
    }

    @Test
    void anUnknownAdultAboveTheCapIsCutDown() {
        AnimalTraits raised = WildCaught.raise(traits(0.9), 1.0, 5L, true, false, DEFAULTS);

        assertEquals(0.4, raised.familiarity(), 1.0e-9);
        assertEquals(Origin.WILD, raised.origin());
    }

    @Test
    void raisingToZeroLeavesTheOriginUnknown() {
        AnimalTraits raised = WildCaught.raise(traits(0.0), 0.0, 5L, true, false, DEFAULTS);

        assertEquals(Origin.UNKNOWN, raised.origin());
    }

    @Test
    void theTamingGraceStaysUnderThePetCap() {
        Tuning lowCap = BarnyardConfig.parse("""
                { "tuning": { "wildPetFamiliarityCap": 0.5 } }
                """).tuning();

        assertEquals(0.5, WildCaught.tamingGrace(0.65, Origin.UNKNOWN, true, lowCap), 1.0e-9);
        assertEquals(0.65, WildCaught.tamingGrace(0.65, Origin.UNKNOWN, true, DEFAULTS), 1.0e-9);
        assertEquals(0.65, WildCaught.tamingGrace(0.65, Origin.UNKNOWN, false, lowCap), 1.0e-9);
    }

    private static AnimalTraits traits(double familiarity) {
        return new AnimalTraits(Sex.FEMALE, 1.0, familiarity, -1L);
    }
}
