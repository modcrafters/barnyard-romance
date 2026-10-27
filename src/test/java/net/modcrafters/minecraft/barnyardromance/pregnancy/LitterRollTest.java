package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.config.Range;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LitterRollTest {
    @Test
    void rollStaysInRangeAndWeightPushesUp() {
        Range range = new Range(2, 5);

        assertEquals(2, LitterRoll.pick(range, 0.5, 0.0));
        assertEquals(5, LitterRoll.pick(range, 0.5, 0.999));
        assertEquals(5, LitterRoll.pick(range, 1.0, 0.5));
        assertEquals(3, LitterRoll.pick(range, 0.0, 0.5));
        for (double weight : new double[] {0.0, 0.5, 1.0}) {
            for (double random : new double[] {0.0, 0.5, 0.999999}) {
                assertEquals(3, LitterRoll.pick(new Range(3, 3), weight, random));
            }
        }
    }

    @Test
    void heavierMothersCarryShorter() {
        Range range = new Range(30_000, 36_000);

        assertEquals(30_000, LitterRoll.pickTicks(range, 1.0, 0.999));
        assertEquals(36_000, LitterRoll.pickTicks(range, 0.0, 0.0));
    }

    @Test
    void anInvertedPairIsSwapped() {
        assertEquals(new Range(2, 5), Range.of(5, 2));
        assertEquals(new Range(2, 5), Range.of(2, 5));
    }
}
