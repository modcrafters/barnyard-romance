package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatingRulesTest {
    @Test
    void onlyAMaleAndAFemaleCanMate() {
        assertTrue(MatingRules.canMate(Optional.of(Sex.MALE), Optional.of(Sex.FEMALE), false, false, true, true));
        assertFalse(MatingRules.canMate(Optional.of(Sex.FEMALE), Optional.of(Sex.FEMALE), false, false, true, true));
        assertFalse(MatingRules.canMate(Optional.of(Sex.MALE), Optional.of(Sex.MALE), false, false, true, true));
    }

    @Test
    void aPregnantFemaleDoesNotMateAgain() {
        assertFalse(MatingRules.canMate(Optional.of(Sex.FEMALE), Optional.of(Sex.MALE), true, false, true, true));
        assertFalse(MatingRules.canMate(Optional.of(Sex.MALE), Optional.of(Sex.FEMALE), false, true, true, true));
    }

    @Test
    void bothAnimalsMustBeFamiliar() {
        assertFalse(MatingRules.canMate(Optional.of(Sex.MALE), Optional.of(Sex.FEMALE), false, false, false, true));
        assertFalse(MatingRules.canMate(Optional.of(Sex.MALE), Optional.of(Sex.FEMALE), false, false, true, false));
        assertTrue(MatingRules.canMate(Optional.of(Sex.FEMALE), Optional.of(Sex.MALE), false, false, true, true));
    }

    @Test
    void animalsWithoutASexAreLeftToVanilla() {
        assertTrue(MatingRules.canMate(Optional.empty(), Optional.of(Sex.FEMALE), false, false, false, false));
    }
}
