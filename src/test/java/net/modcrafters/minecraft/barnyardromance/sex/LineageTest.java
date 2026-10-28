package net.modcrafters.minecraft.barnyardromance.sex;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineageTest {
    private static final UUID MOTHER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID FATHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void unknownHasNoParents() {
        assertNull(Lineage.UNKNOWN.mother());
        assertNull(Lineage.UNKNOWN.father());
        assertFalse(Lineage.UNKNOWN.knows(MOTHER));
    }

    @Test
    void knowsEitherParent() {
        Lineage lineage = new Lineage(MOTHER, FATHER);

        assertTrue(lineage.knows(MOTHER));
        assertTrue(lineage.knows(FATHER));
        assertFalse(lineage.knows(UUID.fromString("00000000-0000-0000-0000-000000000003")));
        assertTrue(new Lineage(MOTHER, null).knows(MOTHER));
    }
}
