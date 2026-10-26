package net.modcrafters.minecraft.barnyardromance.drops;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DropStacksTest {
    @Test
    void aSmallDropStaysOneStack() {
        assertEquals(List.of(5), DropStacks.sizes(5, 64));
    }

    @Test
    void aBigDropSplitsByTheItemsMaxStackSize() {
        assertEquals(List.of(64, 64, 22), DropStacks.sizes(150, 64));
        assertEquals(List.of(16, 16, 3), DropStacks.sizes(35, 16));
    }

    @Test
    void nothingDropsNothing() {
        assertEquals(List.of(), DropStacks.sizes(0, 64));
    }
}
