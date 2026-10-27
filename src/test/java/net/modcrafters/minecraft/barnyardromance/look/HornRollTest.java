package net.modcrafters.minecraft.barnyardromance.look;

import net.modcrafters.minecraft.barnyardromance.age.Aging;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HornRollTest {
    @Test
    void theSameAnimalAlwaysGetsTheSameAnswer() {
        for (long bits = 0L; bits < 200L; bits++) {
            boolean first = HornRoll.femaleHorned(bits * 31L, bits, 0.5);
            assertEquals(first, HornRoll.femaleHorned(bits * 31L, bits, 0.5));
        }
    }

    @Test
    void chanceZeroIsNeverAndOneIsAlways() {
        for (long bits = 0L; bits < 200L; bits++) {
            assertFalse(HornRoll.femaleHorned(bits, ~bits, 0.0));
            assertTrue(HornRoll.femaleHorned(bits, ~bits, 1.0));
        }
    }

    @Test
    void theShareOfHornedFemalesMatchesTheChance() {
        int horned = 0;
        for (long index = 0L; index < 10_000L; index++) {
            if (HornRoll.femaleHorned(0x1234_5678_0000_0000L + index, index, 0.25)) {
                horned++;
            }
        }
        assertEquals(0.25, horned / 10_000.0, 0.02);
    }

    @Test
    void hornsDoNotFollowTheDeathRoll() {
        int differ = 0;
        for (long index = 0L; index < 1_000L; index++) {
            long most = 0x1234_5678_0000_0000L + index;
            boolean byDeath = Aging.spreadRandom(most ^ index) < 0.5;
            if (byDeath != HornRoll.femaleHorned(most, index, 0.5)) {
                differ++;
            }
        }
        assertTrue(differ > 300, "only " + differ + " of 1000 differ");
    }
}
