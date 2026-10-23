package net.modcrafters.minecraft.barnyardromance.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdToolRulesTest {
    @Test
    void setSticksAreCreativeOnly() {
        assertFalse(HerdToolRules.allowed(HerdTool.familiaritySet(30), false));
        assertFalse(HerdToolRules.allowed(HerdTool.pregnancySet(80), false));
        assertTrue(HerdToolRules.allowed(HerdTool.familiaritySet(30), true));
        assertTrue(HerdToolRules.allowed(HerdTool.familiarity(1), false));
        assertTrue(HerdToolRules.allowed(HerdTool.info(), false));
    }

    @Test
    void creativeSticksAreNotUsedUp() {
        assertFalse(HerdToolRules.consumed(HerdTool.familiarity(1), true));
        assertFalse(HerdToolRules.consumed(HerdTool.pregnancySet(50), true));
        assertTrue(HerdToolRules.consumed(HerdTool.familiarity(-1), false));
        assertFalse(HerdToolRules.consumed(HerdTool.info(), false));
    }

    @Test
    void labelsDescribeTheStick() {
        assertEquals("Inspect animal", HerdTool.info().label());
        assertEquals("Familiarity +10%", HerdTool.familiarity(1).label());
        assertEquals("Familiarity -10%", HerdTool.familiarity(-1).label());
        assertEquals("Familiarity 30%", HerdTool.familiaritySet(30).label());
        assertEquals("Pregnancy +10%", HerdTool.pregnancy(1).label());
        assertEquals("Pregnancy 80%", HerdTool.pregnancySet(80).label());
    }
}
