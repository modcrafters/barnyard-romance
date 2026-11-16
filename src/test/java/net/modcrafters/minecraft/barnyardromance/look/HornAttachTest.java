package net.modcrafters.minecraft.barnyardromance.look;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HornAttachTest {
    private static final HornAttach FRESH = new HornAttach(List.of("body", "EMF_body", "EMF_head2"), new Xyz(0, 1, 0));
    private static final HornAttach VANILLA = new HornAttach(List.of("head"), Xyz.ZERO);

    @Test
    void firstPicksFirstPartTheModelHas() {
        Set<List<String>> both = Set.of(FRESH.path(), VANILLA.path());

        assertEquals(FRESH, HornAttach.first(List.of(FRESH, VANILLA), both::contains));
    }

    @Test
    void firstSkipsMissingParts() {
        assertEquals(VANILLA, HornAttach.first(List.of(FRESH, VANILLA), Set.of(VANILLA.path())::contains));
    }

    @Test
    void firstIsNullWhenNoneMatch() {
        assertNull(HornAttach.first(List.of(FRESH, VANILLA), path -> false));
    }
}
