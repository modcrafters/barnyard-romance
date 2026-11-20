package net.modcrafters.minecraft.barnyardromance.core.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModFieldsTest {
    static class Base {
        private int eggTime = 100;
        int other = 5;
    }

    static class Middle extends Base {
    }

    static class Leaf extends Middle {
        int own = 1;
    }

    @Test
    void setsAFieldDeclaredOnASuperclass() {
        Leaf leaf = new Leaf();
        ModFields.holdInt(leaf, "eggTime", 6000);
        assertEquals(6000, ((Base) leaf).eggTime);
        assertEquals(5, leaf.other);
    }

    @Test
    void setsAFieldOnTheClassItself() {
        Leaf leaf = new Leaf();
        ModFields.holdInt(leaf, "own", 9);
        assertEquals(9, leaf.own);
    }

    @Test
    void setsEachInstanceAfterTheFieldIsCached() {
        Leaf first = new Leaf();
        Leaf second = new Leaf();
        ModFields.holdInt(first, "own", 2);
        ModFields.holdInt(second, "own", 3);
        assertEquals(2, first.own);
        assertEquals(3, second.own);
    }

    @Test
    void aMissingFieldGivesUpOnceAndNeverThrows() {
        Leaf leaf = new Leaf();
        assertFalse(ModFields.gaveUp(Leaf.class, "renamed"));
        ModFields.holdInt(leaf, "renamed", 1);
        assertTrue(ModFields.gaveUp(Leaf.class, "renamed"));
        ModFields.holdInt(leaf, "renamed", 1);
        assertTrue(ModFields.gaveUp(Leaf.class, "renamed"));
        assertEquals(1, leaf.own);
    }

    @Test
    void aFieldOfAnotherTypeGivesUp() {
        class Holder {
            String eggTime = "x";
        }
        Holder holder = new Holder();
        ModFields.holdInt(holder, "eggTime", 1);
        assertTrue(ModFields.gaveUp(Holder.class, "eggTime"));
        assertEquals("x", holder.eggTime);
    }

    @Test
    void oneGivenUpFieldDoesNotStopAnother() {
        Leaf leaf = new Leaf();
        ModFields.holdInt(leaf, "missing", 1);
        ModFields.holdInt(leaf, "eggTime", 7);
        assertEquals(7, ((Base) leaf).eggTime);
    }
}
