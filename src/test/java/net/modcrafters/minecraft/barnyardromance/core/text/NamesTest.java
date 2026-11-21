package net.modcrafters.minecraft.barnyardromance.core.text;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NamesTest {
    @Test
    void anAnimalSharingItsNameIsToldApartByItsMod() {
        Map<String, String> names = Map.of("naturalist:duck", "Duck", "untitledduckmod:duck", "Duck", "minecraft:chicken", "Chicken");

        assertEquals(Optional.of("naturalist"), Names.sharedNameMod("naturalist:duck", names));
        assertEquals(Optional.of("untitledduckmod"), Names.sharedNameMod("untitledduckmod:duck", names));
        assertEquals(Optional.empty(), Names.sharedNameMod("minecraft:chicken", names));
    }

    @Test
    void anIdWithNoNamespaceIsMinecrafts() {
        assertEquals("minecraft", Names.modId("chicken"));
        assertEquals("naturalist", Names.modId("naturalist:duck"));
    }
}
