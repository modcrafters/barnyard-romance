package net.modcrafters.minecraft.barnyardromance.advancement;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LaddersTest {
    @Test
    void horseFamilySharesOneLadder() {
        assertEquals(Optional.of("horse"), Ladders.ladderFor("minecraft:horse"));
        assertEquals(Optional.of("horse"), Ladders.ladderFor("minecraft:donkey"));
        assertEquals(Optional.of("horse"), Ladders.ladderFor("minecraft:mule"));
    }

    @Test
    void traderLlamasCountAsLlamas() {
        assertEquals(Optional.of("llama"), Ladders.ladderFor("minecraft:trader_llama"));
    }

    @Test
    void farmAnimalsHaveTheirOwnLadders() {
        for (String species : List.of("cow", "mooshroom", "sheep", "pig", "goat", "llama", "camel", "rabbit", "chicken")) {
            assertEquals(Optional.of(species), Ladders.ladderFor("minecraft:" + species));
        }
    }

    @Test
    void petsAndUnknownSpeciesHaveNoLadder() {
        assertEquals(Optional.empty(), Ladders.ladderFor("minecraft:wolf"));
        assertEquals(Optional.empty(), Ladders.ladderFor("othermod:yak"));
    }

    @Test
    void naturalistLivestockHaveLadders() {
        for (String species : List.of("deer", "boar", "duck", "turkey", "ostrich")) {
            assertEquals(Optional.of(species), Ladders.ladderFor("naturalist:" + species));
        }
    }

    @Test
    void twinsCrossTheStepOnce() {
        assertEquals(List.of(10), Ladders.crossed(9, 11));
        assertEquals(List.of(), Ladders.crossed(10, 11));
        assertEquals(List.of(100), Ladders.crossed(99, 100));
        assertEquals(List.of(10, 100), Ladders.crossed(0, 120));
    }
}
