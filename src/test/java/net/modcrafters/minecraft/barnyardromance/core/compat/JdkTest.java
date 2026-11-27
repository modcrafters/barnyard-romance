package net.modcrafters.minecraft.barnyardromance.core.compat;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// What the helpers promise on every Minecraft version, the Java 21 methods' behaviour included.
class JdkTest {
    @Test
    void clampKeepsAnIntRangeForALong() {
        assertEquals(3, Jdk.clamp(5L, 1, 3));
        assertEquals(1, Jdk.clamp(-5L, 1, 3));
        assertEquals(2, Jdk.clamp(2L, 1, 3));
    }

    @Test
    void clampKeepsALongRange() {
        assertEquals(10L, Jdk.clamp(50L, 0L, 10L));
        assertEquals(0L, Jdk.clamp(-50L, 0L, 10L));
    }

    @Test
    void clampKeepsADoubleAndFloatRange() {
        assertEquals(1.0, Jdk.clamp(1.5, 0.0, 1.0));
        assertEquals(0.0, Jdk.clamp(-0.5, 0.0, 1.0));
        assertEquals(0.5F, Jdk.clamp(0.5F, 0.0F, 1.0F));
        assertEquals(1.0F, Jdk.clamp(2.0F, 0.0F, 1.0F));
    }

    @Test
    void clampRefusesAnInvertedRange() {
        assertThrows(IllegalArgumentException.class, () -> Jdk.clamp(1.5, 2.0, 1.0));
    }

    @Test
    void firstAndLastRefuseAnEmptyList() {
        assertThrows(NoSuchElementException.class, () -> Jdk.first(List.of()));
        assertThrows(NoSuchElementException.class, () -> Jdk.last(new ArrayList<>()));
    }

    @Test
    void firstAndLastReadTheEnds() {
        assertEquals(1, Jdk.first(List.of(1, 2)));
        assertEquals(2, Jdk.last(List.of(1, 2)));
    }

    @Test
    void removeLastAndRemoveFirstTakeAnEndOff() {
        List<Integer> list = new ArrayList<>(List.of(1, 2));
        assertEquals(2, Jdk.removeLast(list));
        assertEquals(List.of(1), list);
        assertEquals(1, Jdk.removeFirst(list));
        assertEquals(List.of(), list);
        assertThrows(NoSuchElementException.class, () -> Jdk.removeFirst(list));
        assertThrows(NoSuchElementException.class, () -> Jdk.removeLast(list));
    }

    @Test
    void reversedIsAViewOfTheList() {
        List<Integer> list = new ArrayList<>(List.of(1, 2));
        List<Integer> reversed = Jdk.reversed(list);
        assertEquals(List.of(2, 1), reversed);
        list.add(3);
        assertEquals(List.of(3, 2, 1), reversed);
    }

    @Test
    void repeatAppendsTheTextThatManyTimes() {
        assertEquals("abbb", Jdk.repeat(new StringBuilder("a"), "b", 3).toString());
        assertEquals("a", Jdk.repeat(new StringBuilder("a"), "b", 0).toString());
    }
}
